# Tachistoscop 3.0 alpha

MVP Android simplificat pentru antrenarea percepției rapide și a memoriei de scurtă durată.

## Fluxul unei probe

1. Fixare centrală `+` — 650 ms.
2. Expunere — 50–1000 ms.
3. Mască vizuală — 100 ms.
4. Retenție — imediat, 0.25, 0.5, 1, 2, 4 sau 8 secunde.
5. Reproducere tastată.
6. Scor și următoarea probă.

## Cantitate

- 1 literă
- 1 cuvânt
- 2–10 cuvinte

Limita maximă este intenționat 10 cuvinte per expunere.

## Moduri

### Memorie instant
Scorul principal combină cuvintele reținute și ordinea lor. Se afișează separat și similaritatea literală.

### Ad litteram
Scorul principal este similaritatea caracter-cu-caracter, iar reproducerea identică primește 100%.

## AUTO

- 3 rezultate consecutive >= 90% cresc dificultatea.
- 2 rezultate consecutive < 60% reduc dificultatea.
- Ordinea creșterii: cantitate → expunere mai scurtă → retenție mai lungă.

## Build

Necesită Java 17, Gradle și Android SDK 36.

```bash
gradle --no-daemon :app:assembleDebug
```
