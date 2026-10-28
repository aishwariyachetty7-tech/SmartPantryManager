# Smart Pantry Manager

## Description:
Smart Pantry Manager is an Android application developed in Java that helps users reduce food waste. It allows users to track their pantry ingredients and suggests recipes they can cook using **strictly** the ingredients they currently have at home. 

## Database Choice:
I chose **SQLite** (using `SQLiteOpenHelper`) for this application. SQLite is well suited for a local, offline-first time full app like this. It allowed me to create a relational structure (Pantry Items, Recipes, Recipe Ingredients) and execute complex raw queries to handle the strict-matching logic entirely on-device without needing an internet connection or a backend server.

## Features:
* There are full CRUD operations for Pantry Items (Create, Read, Update, Delete).
* 15 Pre-loaded recipes with ingredients and steps.
* A strict matching algorithm, which ignores plurals and unit variations.
* A dynamic UI using RecyclerView and Custom Adapters.
* A settings screen using SharedPreferences.

## Setup / Run Instructions:
1. Clone this repository: `git clone "https://github.com/aishwariyachetty7-tech/SmartPantryManager.git"`
2. Open the project in **Android Studio**.
3. Let Gradle sync finish.
4. Select an emulator (min SDK 24) or connect a physical Android device via Bluetooth or cable.
5. Click the **Run (▶)** button to build and install the app.
