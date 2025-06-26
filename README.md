Jogo de Avião 2D — Android

Este projeto é um jogo 2D de avião desenvolvido em Java para a plataforma Android. O principal objetivo foi aplicar conceitos de desenvolvimento mobile com SurfaceView, controle de jogo, pontuação e persistência de dados com banco de dados SQLite.

📱 Funcionalidades
	•	Gameplay 2D com SurfaceView
Jogador controla um avião que deve desviar de inimigos que descem verticalmente da parte superior da tela.
	•	Pontuação e Recorde
O jogo exibe a pontuação atual durante a partida e, ao final (Game Over), mostra um popup com a pontuação final e o recorde armazenado no banco de dados.
	•	Sistema de Jogador
Cada jogador possui um nome e um ID, armazenados em SharedPreferences e utilizados para salvar e recuperar a pontuação.
	•	Banco de Dados SQLite
Utilizado para armazenar os dados de jogadores e suas pontuações. O sistema salva automaticamente o melhor resultado ao final da partida.
	•	Menu de Pausa
A qualquer momento, o jogador pode pausar a partida e optar por continuar o jogo.
	•	Tela de Perfil (opcional)
Há suporte para uma tela de perfil onde o jogador pode visualizar seus dados (ID e nome).

🛠 Tecnologias Utilizadas
	•	Java
	•	Android SDK
	•	SurfaceView
	•	SQLite (via SQLiteOpenHelper)
	•	SharedPreferences
	•	Material Components (FloatingActionButton, AlertDialog)

🧪 Testes Realizados
	•	Testado em emuladores Android de diferentes tamanhos de tela.
	•	Verificado persistência correta da pontuação.
	•	Game Over funcional com opções de “Continuar” e “Sair”.
	•	Menu de pausa ativado via botão flutuante.

🔄 Melhorias Futuras
	•	Adicionar sons ao jogo.
	•	Implementar fases ou níveis de dificuldade.
	•	Adicionar itens bônus ou power-ups.
	•	Melhorar colisão com mais precisão (bounding box ou pixel-perfect).
