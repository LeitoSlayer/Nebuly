# App Nebuly

Nebuly es una aplicación educativa desarrollada en **Android Studio**, diseñada para que los usuarios aprendan sobre los **planetas y el universo** de manera progresiva e interactiva.  
A medida que avanzas en tu aprendizaje, puedes ganar monedas, desbloquear logros y adquirir íconos personalizados en la tienda.

---

## Contribuidores

- Jaraba Pérez Leo  
- Pachón Suárez Jesús  
- Rodríguez González Edgar David  
- Rojas Camargo Valentina  
- Rojas Pardo Marilyn Sophia Valentina  

---

## Características

Nebuly está estructurada en **5 módulos principales**, cada uno con un propósito específico:

1. **Inicio de sesión**  
   Permite a los usuarios crear cuentas, iniciar sesión y mantener su progreso sincronizado en la nube mediante **Firebase Authentication**.

2. **Tienda**  
   Ofrece una sección donde los usuarios pueden comprar íconos usando monedas obtenidas al completar actividades.

3. **Logros**  
   Sistema de **gamificación** que motiva el estudio continuo. Cada logro se obtiene al completar lecciones dentro de la app.

4. **Aprender**  
   Módulo educativo que organiza rutas de aprendizaje progresivas sobre los **planetas del sistema solar**.  

5. **Investigar**  
   Espacio interactivo donde puedes visualizar **modelos 3D** de planetas y cuerpos celestes, explorando sus características en detalle.

---

## Estructura del proyecto

```
app/
│
├── manifests/
│   └── AndroidManifest.xml
│
├── kotlin+java/
│   └── com.utadeo.nebuly/
│       ├── components/              
│       │   ├── AchievementCard.kt
│       │   ├── AchievementUnlockedNotification.kt
│       │   ├── ActionButton.kt
│       │   ├── BackButton.kt
│       │   ├── StartButton.kt
│       │   ├── MenuCart.kt
│       │   ├── TitleHeader.kt
│       │   └── UserHeader.kt
│       │
│       ├── data/
│       │   ├── models/              
│       │   └── navigation/         
│       │       └── AppNavigation.kt
│       │
│       ├── screens/                
│       │   ├── avatar/
│       │   ├── viewer/
│       │   ├── AchievementsScreen.kt
│       │   ├── AvatarDetailScreen.kt
│       │   ├── ComienzoScreen.kt
│       │   ├── LoginScreen.kt
│       │   ├── MenuScreen.kt
│       │   ├── NivelesScreen.kt
│       │   ├── PlanetDetailScreen.kt
│       │   ├── QuestionScreen.kt
│       │   ├── RegisterScreen.kt
│       │   ├── RutaAprendizajeScreen.kt
│       │   ├── StoreScreen.kt
│       │   ├── WelcomeActivity.kt
│       │   └── WelcomeScreen.kt
│       │
│       ├── ui/                     
│       ├── utils/                   
│       └── MainActivity.kt          
│
└── res/                             
```


## Tecnologías utilizadas
- Kotlin
- Android Studio
- Firebase Authentication
- Firebase Firestore
- Material Design 3
- SceneView
- Jetpack Compose
- Gradle

## Instalación

-Paso 1: Clonar el repositorio\
 https://github.com/LeitoSlayer/Nebuly.git

-Paso 2:\
Abre Android Studio\
Seleccionar File → Open\
Selecciona la carpeta Nebuly\
Espera a que Gradle sincronice automáticamente los módulos.
- Rama principal **master** 

-Paso 3:\
Para descargar el APK de la aplicación ingresar al siguiente link de drive\
https://drive.google.com/drive/folders/1rpXR-HOSmX7Q03zCZcsYNXfZzAwz4eBX?usp=drive_link
