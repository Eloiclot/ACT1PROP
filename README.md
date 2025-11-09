**Autors:**
- Eloi Clot
- Marc Ortiz

****Link:**** https://github.com/Eloiclot/ACT1PROP.git


*****Explicació Heurística avançada:*****
La diferència respecte a l’heurística bàsica recau en el fet que en lloc de regir-se per Manhattan, executem una cerca BFS (cerca d’amplada) directament sobre el mapa, calculant així la ruta més curta fins a l’objectiu prioritari.
El cas però, és que en la nostra BSF sí que respectem els obstacles com parets i portes tancades, fent que treballem amb una estimació més propera al cost real final, permetent descartar camins curts “falsos”.
