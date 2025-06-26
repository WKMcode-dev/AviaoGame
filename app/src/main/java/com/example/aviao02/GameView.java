package com.example.aviao02;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import java.util.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private long playerId = -1;
    private String playerName = null;

    private final SurfaceHolder holder;
    private final Paint paint;
    private Thread gameThread;
    private volatile boolean isRunning = false;

    private GameActivity gameActivity;

    private Bitmap fundoEscalado, aviaoBitmap, inimigoBitmap;
    private float fundoY1 = 0, fundoY2;

    private float aviaoX, aviaoY, destinoX, destinoY;

    private final List<Enemy> inimigos = new ArrayList<>();
    private final Random random = new Random();

    private long ultimoSpawn = 0;

    private static final int MAX_INIMIGOS = 7;
    private float MIN_DISTANCE_BETWEEN_ENEMIES = 150f;

    private int currentScore = 0;
    private boolean gameOver = false;
    private boolean isPausedForLogic = false;
    private boolean scoreSaved = false;

    // --- Construtores encadeados ---
    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.holder = getHolder();
        this.holder.addCallback(this);
        this.paint = new Paint();
        checkContext(context);
        setFocusable(true);
    }

    public GameView(Context context, long playerId, String playerName) {
        this(context);
        this.playerId = playerId;
        this.playerName = playerName;
    }

    private void checkContext(Context context) {
        if (context instanceof GameActivity) {
            this.gameActivity = (GameActivity) context;
        } else {
            Log.e("GameView", "Contexto inválido: use GameActivity.");
        }
    }

    // --- SurfaceHolder.Callback ---
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        carregarRecursos();

        if (aviaoBitmap != null) {
            MIN_DISTANCE_BETWEEN_ENEMIES = aviaoBitmap.getWidth() * 1.5f;
        }

        start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stop();
    }

    // --- Carregamento de recursos ---
    private void carregarRecursos() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap fundo = BitmapFactory.decodeResource(getResources(), R.drawable.fundo_espaco, options);
        if (fundo != null) {
            fundoEscalado = Bitmap.createScaledBitmap(fundo, getWidth(), getHeight(), false);
            fundoY2 = -fundoEscalado.getHeight();
        }

        Bitmap tempAviao = BitmapFactory.decodeResource(getResources(), R.drawable.air_player, options);
        if (tempAviao != null) {
            int largura = getWidth() / 5;
            float aspect = (float) tempAviao.getHeight() / tempAviao.getWidth();
            int altura = (int) (largura * aspect);

            aviaoBitmap = Bitmap.createScaledBitmap(tempAviao, largura, altura, false);
            aviaoX = (getWidth() - largura) / 2f;
            aviaoY = getHeight() * 0.75f - altura / 2f;
            destinoX = aviaoX;
            destinoY = aviaoY;

            Bitmap tempInimigo = BitmapFactory.decodeResource(getResources(), R.drawable.air_enemy, options);
            if (tempInimigo != null) {
                inimigoBitmap = Bitmap.createScaledBitmap(tempInimigo, largura, altura, false);
            }
        }
    }

    // --- Loop principal do jogo ---
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1_000_000_000.0 / 60.0;
        double delta = 0;

        long timer = System.currentTimeMillis();
        int frames = 0, ticks = 0;

        while (isRunning) {
            if (!holder.getSurface().isValid()) continue;

            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                if (!isPausedForLogic) updateGameLogic();
                ticks++;
                delta--;
            }

            renderGame();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                Log.d("GameView", "TPS: " + ticks + " | FPS: " + frames);
                ticks = 0;
                frames = 0;
                timer += 1000;
            }
        }
    }

    // --- Atualização da lógica ---
    private void updateGameLogic() {
        if (inimigoBitmap == null || fundoEscalado == null) return;

        // Scroll fundo
        float scrollVel = 5f;
        fundoY1 += scrollVel;
        fundoY2 += scrollVel;

        int altura = fundoEscalado.getHeight();
        if (fundoY1 >= altura) fundoY1 = fundoY2 - altura;
        if (fundoY2 >= altura) fundoY2 = fundoY1 - altura;

        // Se jogo acabou, só salva score uma vez e mostra dialog
        if (gameOver) {
            if (!scoreSaved && gameActivity != null) {
                // Salva a pontuação do jogador (no banco de dados, por exemplo)
                gameActivity.savePlayerScore(currentScore);

                // Acessa o SharedPreferences para pegar e salvar o recorde (high score)
                SharedPreferences prefs = gameActivity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                int highScore = prefs.getInt("high_score", 0);

                // Se o jogador bateu o recorde, salva o novo valor
                if (currentScore > highScore) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("high_score", currentScore);
                    editor.apply();
                    highScore = currentScore; // atualiza a variável para exibir corretamente
                }

                // Mostra o diálogo de game over com a pontuação e o recorde
                gameActivity.mostrarGameOverDialog(currentScore, highScore);

                // Marca que o score já foi salvo para não repetir
                scoreSaved = true;
                Log.d("GameView", "Pontuação salva para ID: " + playerId);
            }
            return; // Encerra o update enquanto o jogo estiver no estado de Game Over
        }


        // Movimento suave do avião para destino
        float smooth = 0.1f;
        aviaoX += (destinoX - aviaoX) * smooth;
        aviaoY += (destinoY - aviaoY) * smooth;

        // Spawn inimigos
        long agora = System.currentTimeMillis();
        if (inimigos.size() < MAX_INIMIGOS && (agora - ultimoSpawn > getDynamicSpawnInterval())) {
            float posX;
            int tentativas = 0;
            int maxTentativas = 10;

            do {
                posX = random.nextInt(getWidth() - inimigoBitmap.getWidth());
                tentativas++;
            } while (Math.abs(posX - lastSpawnX) < MIN_DISTANCE_BETWEEN_ENEMIES && tentativas < maxTentativas);

            inimigos.add(new Enemy(inimigoBitmap, posX, -inimigoBitmap.getHeight(), 10f));
            ultimoSpawn = agora;
            lastSpawnX = posX;
        }

        // Atualiza inimigos e checa colisões
        Iterator<Enemy> it = inimigos.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.update();

            if (checkCollisionWithPlayer(e)) {
                gameOver = true;
            }

            if (e.isOutOfScreen(getHeight())) {
                it.remove();
                continue;
            }
        }

        // Atualiza score
        currentScore++;
    }

    private float lastSpawnX = -1000f;

    // Calcula intervalo dinâmico para spawnar inimigos (mais rápido conforme score cresce)
    private long getDynamicSpawnInterval() {
        int base = Math.max(700, 2000 - (currentScore / 50) * 100);
        int variacao = random.nextInt(600) - 300; // +-300ms
        return base + variacao;
    }

    // Verifica colisão entre avião e inimigo usando Paths triangulares
    private boolean checkCollisionWithPlayer(Enemy enemy) {
        Path playerHitbox = getPlayerHitbox();
        Path enemyHitbox = getEnemyHitbox(enemy);

        Region playerRegion = new Region();
        Region enemyRegion = new Region();

        RectF playerBounds = new RectF();
        playerHitbox.computeBounds(playerBounds, true);
        playerRegion.setPath(playerHitbox, new Region(
                (int) playerBounds.left, (int) playerBounds.top,
                (int) playerBounds.right, (int) playerBounds.bottom));

        RectF enemyBounds = new RectF();
        enemyHitbox.computeBounds(enemyBounds, true);
        enemyRegion.setPath(enemyHitbox, new Region(
                (int) enemyBounds.left, (int) enemyBounds.top,
                (int) enemyBounds.right, (int) enemyBounds.bottom));

        return playerRegion.op(enemyRegion, Region.Op.INTERSECT);
    }

    // Hitbox do avião em forma de triângulo apontando pra cima
    private Path getPlayerHitbox() {
        float centerX = aviaoX + aviaoBitmap.getWidth() / 2f;
        float topY = aviaoY;
        float bottomY = aviaoY + aviaoBitmap.getHeight();

        float baseWidth = aviaoBitmap.getWidth() * 0.55f;
        float leftX = centerX - baseWidth / 2f;
        float rightX = centerX + baseWidth / 2f;

        Path path = new Path();
        path.moveTo(centerX, topY);          // ponta do triângulo (cima)
        path.lineTo(leftX, bottomY);         // base esquerda
        path.lineTo(rightX, bottomY);        // base direita
        path.close();

        return path;
    }

    // Hitbox do inimigo em forma de triângulo apontando para baixo
    private Path getEnemyHitbox(Enemy enemy) {
        float ex = enemy.getX();
        float ey = enemy.getY();
        float width = enemy.getBitmap().getWidth();
        float height = enemy.getBitmap().getHeight();

        float centerX = ex + width / 2f;
        float topY = ey;

        float baseWidth = width * 0.55f;
        float leftX = centerX - baseWidth / 2f;
        float rightX = centerX + baseWidth / 2f;

        Path path = new Path();
        path.moveTo(centerX, ey + height);  // ponta do triângulo (baixo)
        path.lineTo(leftX, topY);            // base esquerda
        path.lineTo(rightX, topY);           // base direita
        path.close();

        return path;
    }

    // --- Renderização ---
    private void renderGame() {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) drawOnCanvas(canvas);
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawOnCanvas(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        if (fundoEscalado != null) {
            canvas.drawBitmap(fundoEscalado, 0, fundoY1, null);
            canvas.drawBitmap(fundoEscalado, 0, fundoY2, null);
        }

        for (Enemy enemy : inimigos) {
            enemy.draw(canvas);
        }

        if (aviaoBitmap != null) {
            canvas.drawBitmap(aviaoBitmap, aviaoX, aviaoY, null);
        }

        // HUD
        paint.setColor(Color.WHITE);
        paint.setTextSize(40f);
        canvas.drawText("Score: " + currentScore, 50, 70, paint);

        if (playerName != null) {
            canvas.drawText("Jogador: " + playerName, 50, 120, paint);
        }
        canvas.drawText("ID: " + playerId, 50, 170, paint);

        // Game over ou pausa
        if (gameOver || isPausedForLogic) {
            paint.setTextSize(80f);
            paint.setTextAlign(Paint.Align.CENTER);
            String texto = gameOver ? "GAME OVER" : "PAUSADO";
            int x = canvas.getWidth() / 2;
            int y = canvas.getHeight() / 2;
            canvas.drawText(texto, x, y, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }

    // --- Controle de toque ---
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (aviaoBitmap == null || gameOver) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            destinoX = event.getX() - aviaoBitmap.getWidth() / 2f;
            destinoY = event.getY() - aviaoBitmap.getHeight() / 2f;
        }

        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            performClick();
        }

        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    // --- Controle do jogo ---
    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;
        isPausedForLogic = false;
        gameOver = false;
        scoreSaved = false;
        currentScore = 0;

        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;

        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("GameView", "Erro ao parar thread: " + e.getMessage());
            }
            gameThread = null;
        }
    }

    public void pauseGameLogic() {
        isPausedForLogic = true;
    }

    public void resumeGameLogic() {
        isPausedForLogic = false;
        if (!isRunning) start();
    }

    public void resetGame() {
        stop();
        inimigos.clear();
        currentScore = 0;
        gameOver = false;
        scoreSaved = false;
        isPausedForLogic = false;

        if (aviaoBitmap != null) {
            aviaoX = (getWidth() - aviaoBitmap.getWidth()) / 2f;
            aviaoY = getHeight() * 0.75f - aviaoBitmap.getHeight() / 2f;
            destinoX = aviaoX;
            destinoY = aviaoY;
        }

        start();
    }
}
