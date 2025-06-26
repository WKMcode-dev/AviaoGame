# ✈️ Jogo de Avião 2D — Android

Faculdade de Tecnologia e Inovação Senac-DF
Alunos: Guilherme de Morais / Wryell Kenneth Machado(WKM) / Eduardo Oliveira

Este projeto é um **jogo 2D de avião** desenvolvido em **Java para Android**, utilizando `SurfaceView` para renderização gráfica. O jogador controla um avião e deve evitar colisões com inimigos, enquanto acumula pontos. O projeto explora conceitos de desenvolvimento mobile, controle de jogo, persistência de dados com `SQLite` e personalização de usuário com `SharedPreferences`.

---

## 📱 Funcionalidades

- 🎮 **Gameplay com SurfaceView**  
  Jogador controla um avião que deve desviar de inimigos que descem verticalmente.

- 🏆 **Pontuação e Recorde**  
  Exibe pontuação atual durante a partida e, ao final, mostra um *popup* com a pontuação final e o recorde salvo no banco de dados.

- 👤 **Sistema de Jogador**  
  Cada jogador possui um `ID` e um `nome`, armazenados via `SharedPreferences`.

- 🗃️ **Banco de Dados SQLite**  
  Utilizado para armazenar os dados dos jogadores e salvar automaticamente seus melhores resultados.

- ⏸️ **Menu de Pausa**  
  Jogador pode pausar a partida a qualquer momento e optar por continuar.

- 🧾 **Tela de Perfil (opcional)**  
  Permite visualizar dados do jogador (ID e nome).

---

## 🛠 Tecnologias Utilizadas

- Java  
- Android SDK  
- `SurfaceView`  
- `SQLite` (`SQLiteOpenHelper`)  
- `SharedPreferences`  
- Material Components (`FloatingActionButton`, `AlertDialog`)

---

## 🧪 Testes Realizados

- ✅ Testado em diversos emuladores Android com diferentes tamanhos de tela  
- ✅ Persistência correta de pontuações verificada  
- ✅ Funcionamento esperado do popup de **Game Over** com as opções "Continuar" e "Sair"  
- ✅ Menu de pausa acessado via botão flutuante

---

## 🔄 Melhorias Futuras

- 🔊 Adicionar efeitos sonoros e música de fundo  
- 🧩 Implementar fases ou níveis de dificuldade  
- ⭐ Adicionar itens bônus ou *power-ups*  
- 🎯 Melhorar sistema de colisão com bounding box ou pixel-perfect collision

---

Feito com dedicação e aprendizado 🚀  
