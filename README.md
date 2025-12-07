# 📱 Aplikacja Mobilna do wspomagania segregacji odpadów

Mobilna wersja systemu do segregacji odpadów, działająca na systemie Android. Aplikacja wykorzystuje wbudowany model sieci neuronowej (**TensorFlow Lite**)
do rozpoznawania odpadów w czasie rzeczywistym, bez konieczności dostępu do Internetu.

Projekt został stworzony jako dodatek do pracy inżynierskiej, której tematem była Aplikacja webowa do segregacji odpadów i edukacji ekologicznej

## 🚀 Funkcjonalności

* **Rozpoznawanie obrazu:** Klasyfikacja odpadów na podstawie zdjęcia z kamery lub galerii.
* **Działanie Offline:** Model predykcyjny (ResNet50V2) jest zaszyty wewnątrz aplikacji (`lite_model_v2.tflite`).
* **Natychmiastowy wynik:** Użytkownik otrzymuje informację o tym jakiego rodzaju jest przesłany odpad (np. Papier, Szkło) oraz do którego koloru pojemnika go wyrzucić.
* **Wybieranie i robienie zdjęć:** Użytkownik ma możliwość przesłania zdjęcia z galerii lub zrobienia go bezpośrednio w aplikacji.

## 🛠 Wymagania do uruchomienia aplikacji

* **Android Studio:** Wersja Otter 2.
* **Telefon z Androidem:** System Android 7.0 (API 24) lub nowszy.
* **Kabel USB:** Do podłączenia telefonu w trybie debugowania.

## ⚙️ Instrukcja uruchomienia

Aby uruchomić projekt lokalnie, wykonaj poniższe kroki:

### 1. Klonowanie repozytorium
```bash
git clone https://github.com/MK396/praca_aplikacja_mobilna.git
```

### 2. Otwarcie w Android Studio

1. Uruchom Android Studio
2. Wybierz opcję Open i wybierz folder gdzie sklonowano aplikację
3. Poczekaj, aż Gradle pobierze wszystkie zależności (pasek w prawym dolnym ekranie)

### 3. Konfiguracja telefonu

1. Wejdź w ustawienia telefonu i znajdź sekcję "O telefonie"
2. Kliknij kilka razy w numer nakładki systemowej (w telefonach xiaomi to wersja MIUI)
3. Pojawi się komunikat, że zostałeś programistą
4. Znajdź dodatkowe ustawienia i wejdź w opcje programistyczne
5. Włącz opcję Włącz opcję **Debugowanie USB**
6. Podłącz telefon do komputera kablem USB.
7. Na ekranie telefonu pojawi się komunikat "Zezwalać na debugowanie USB?". Zaznacz **Zezwól**.

### 4. Uruchom aplikację
1.  Otwórz projekt w **Android Studio**.
2.  Na górnym pasku, obok zielonego przycisku "Run", rozwiń listę urządzeń. Twój telefon powinien być tam widoczny (zamiast emulatora).
3.  Wybierz swój telefon i kliknij **Run (▶)**.
4.  Aplikacja zostanie zbudowana, zainstalowana i automatycznie uruchomiona na Twoim telefonie.

## Szybka instalacja (plik APK)

Jeśli nie chcesz korzystać z Android Studio, możesz zainstalować gotową aplikację bezpośrednio na telefonie.

1.  Pobierz plik `rozpoznawanie_odpadów.apk`, który znajduje się w folderze apk.
2.  Prześlij plik na telefon (przez kabel USB, e-mail lub Google Drive).
3.  W telefonie otwórz menedżer plików i kliknij w pobrany plik.
4.  Jeśli telefon zapyta o zgodę na instalację z nieznanych źródeł – **zezwól**.
5.  Po zakończeniu instalacji ikona aplikacji pojawi się w menu telefonu.