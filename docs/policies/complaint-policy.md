# Polityka reklamacji (przykładowy dokument startowy)

> **Status:** Przykładowy dokument źródłowy dla MVP Hardware Service Decision Copilot.
> Koduje reguły, które agent decyzyjny musi stosować dla wniosków typu **COMPLAINT** (reklamacja / wada).
> Przed jakimkolwiek użyciem produkcyjnym należy go zastąpić rzeczywistą polityką firmy.

---

## 1. Zakres

Niniejsza polityka reguluje **reklamacje**, w których klient zgłasza, że produkt jest **wadliwy,
uszkodzony lub niesprawny** i ubiega się o naprawę, wymianę lub zwrot pieniędzy w ramach gwarancji.
Dobrowolne zwroty bez wady obsługiwane są przez odrębną **Politykę zwrotów**.

## 2. Okno uprawniające do reklamacji

- Reklamacje są przyjmowane w ciągu **24 miesięcy** od daty zakupu (standardowy okres gwarancji).
- Wnioski po 730. dniu są **nieuprawnione** i muszą zostać odrzucone (REJECT).
- Jeśli data zakupu jest brakująca lub przyszła, sprawę należy eskalować (ESCALATE).

## 3. Objęte vs nieobjęte gwarancją

### Objęte (wada — kwalifikuje się do APPROVE)
- Wady produkcyjne: martwe piksele, awaria baterii, awaria podzespołów, błędy oprogramowania układowego.
- Uszkodzenia powstałe bez zewnętrznego uderzenia, zgodne z wadą materiałową/wykonania.
- Awaria funkcjonalna podczas normalnego, zgodnego z przeznaczeniem użytkowania.

### Nieobjęte (z winy klienta — kwalifikuje się do REJECT)
- **Uszkodzenia fizyczne / mechaniczne**: pęknięty ekran, wgniecenia, wygięta ramka, uszkodzone porty wskutek upadków lub nacisku.
- **Uszkodzenia od cieczy**: korozja, ślady zalania, uruchomione wskaźniki wilgoci.
- **Niewłaściwe użytkowanie / nieautoryzowana naprawa**: ingerencja osób trzecich, otwarta obudowa, niezatwierdzone części.
- **Normalne zużycie eksploatacyjne**: kosmetyczne otarcia niewpływające na działanie.

## 4. Ocena dowodów

Zdjęcie jest podstawowym dowodem. Agent musi ocenić:

1. **Czy produkt jest uszkodzony i w jaki sposób?** (rodzaj i lokalizacja uszkodzenia)
2. **Jaka jest najbardziej prawdopodobna przyczyna?** (wada produkcyjna vs uderzenie/ciecz/niewłaściwe użytkowanie)
3. **Czy ta przyczyna jest objęta** zgodnie z sekcją 3?

Podany **powód reklamacji** (obowiązkowy tekst od klienta) musi zostać zestawiony z
dowodami wizualnymi. Jeśli podany powód jest sprzeczny ze zdjęciem, należy eskalować (ESCALATE).

## 5. Mapowanie decyzji

| Wynik | Warunek |
|---|---|
| **APPROVE** | W oknie gwarancyjnym **oraz** uszkodzenie/wada jest zgodne z wadą objętą gwarancją. |
| **REJECT** | Poza oknem, **lub** uszkodzenie jest wyraźnie z winy klienta (uderzenie/ciecz/niewłaściwe użytkowanie/zużycie). |
| **ESCALATE** | Przyczyna niejednoznaczna, zdjęcie niejednoznaczne/rozmyte/niewłaściwy przedmiot, podany powód sprzeczny ze zdjęciem albo data brakująca/nieprawidłowa. |

## 6. Wymagane informacje dla klienta

- Wynik **APPROVE** potwierdza przyjęcie reklamacji i rozpoczęcie procesu serwisowego.
- Wynik **REJECT** lub **ESCALATE** jest **wstępny** i podlega weryfikacji przez specjalistę
  przed uznaniem go za ostateczny.
