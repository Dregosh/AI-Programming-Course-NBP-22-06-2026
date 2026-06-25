# Polityka zwrotów (przykładowy dokument startowy)

> **Status:** Przykładowy dokument źródłowy dla MVP Hardware Service Decision Copilot.
> Koduje reguły, które agent decyzyjny musi stosować dla wniosków typu **RETURN** (zwrot).
> Przed jakimkolwiek użyciem produkcyjnym należy go zastąpić rzeczywistą polityką firmy.

---

## 1. Zakres

Niniejsza polityka reguluje **dobrowolne zwroty** elektroniki zakupionej w firmie,
gdy klient chce odesłać produkt w celu uzyskania zwrotu pieniędzy **bez zgłaszania wady**.
Roszczenia z tytułu wad obsługiwane są przez odrębną **Politykę reklamacji**.

## 2. Okno uprawniające do zwrotu

- Zwroty są przyjmowane w ciągu **30 dni kalendarzowych** od daty zakupu.
- Wnioski złożone po 30. dniu są **nieuprawnione** i muszą zostać odrzucone (REJECT).
- Jeśli data zakupu jest brakująca lub przyszła, sprawę należy eskalować (ESCALATE).

## 3. Wymagania dotyczące stanu (standard odsprzedaży)

Zwrócony produkt kwalifikuje się do zwrotu pieniędzy **tylko wtedy, gdy może zostać ponownie sprzedany jako nowy**. Produkt musi wykazywać:

- **Brak śladów użytkowania**: bez zarysowań, otarć, śladów zużycia, odcisków palców, wnikającego kurzu czy zabrudzeń.
- **Brak uszkodzeń fizycznych**: bez pęknięć, wgnieceń, wygiętych elementów, uszkodzonych portów czy wad kosmetycznych.
- **Brak modyfikacji**: bez naklejek innych firm, grawerów czy wymienionych podzespołów.
- Stan oryginalny, odpowiadający produktowi nieotwartemu lub jak nowy.

Jeśli zdjęcie wykazuje **jakikolwiek** ślad użytkowania lub uszkodzenia, zwrot **nie** spełnia
standardu odsprzedaży i musi zostać odrzucony (REJECT) (klient może zamiast tego złożyć reklamację, jeśli występuje wada).

## 4. Uwagi dotyczące kategorii

- **Smartfon / Tablet / Laptop / Smartwatch**: ekrany i obudowy muszą być wolne od zarysowań;
  brak blokad aktywacyjnych i zalogowanych kont osobistych.
- **Słuchawki**: wkładki douszne / nausznice muszą być nieużywane ze względów higienicznych.
- Akcesoria wrażliwe higienicznie noszące ślady użytkowania **nie** podlegają zwrotowi.

## 5. Mapowanie decyzji

| Wynik | Warunek |
|---|---|
| **APPROVE** | W oknie 30 dni **oraz** zdjęcie nie wykazuje śladów użytkowania ani uszkodzeń (nadaje się do odsprzedaży). |
| **REJECT** | Poza oknem, **lub** zdjęcie wykazuje jakiekolwiek użytkowanie/uszkodzenie, **lub** niespełniona reguła higieniczna dla kategorii. |
| **ESCALATE** | Zdjęcie niejednoznaczne/rozmyte/niewłaściwy przedmiot, data zakupu brakująca lub nieprawidłowa, albo dowody są sprzeczne. |

## 6. Wymagane informacje dla klienta

- Wynik **APPROVE** potwierdza przyjęcie zwrotu i rozpoczęcie procesu zwrotu pieniędzy.
- Wynik **REJECT** lub **ESCALATE** jest **wstępny** i podlega weryfikacji przez specjalistę
  przed uznaniem go za ostateczny.
