package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Direccio;
import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

import java.util.*;

/**
 * Heuristica avançada:
 * Calcula la distància de la ruta real (usant un BFS intern sobre la graella)
 * fins a la clau més propera que quedi per recollir.
 * Si totes les claus estan recollides, calcula la distància real a la sortida.
 * Aquesta heurística SÍ respecta parets i portes tancades.
 */
public class HeuristicaAvancada implements Heuristica {
    
    @Override
    public int h(Mapa estat) {
        
        //@TODO: reemplaceu tot el codi per la vostra heurística.
        
        if(estat.esMeta()) {
            return 0;
        }

        // 1. Trobar tots els objectius (claus pendents o sortida)
        Collection<Posicio> objectius = new ArrayList<>();
        
        for (int i = 0; i < estat.getN(); i++) {
            for (int j = 0; j < estat.getM(); j++) {
                int cell = estat.getCell(i, j);
                if (Character.isLowerCase(cell)) {
                    if (!estat.teClau((char) cell)) {
                        objectius.add(new Posicio(i, j));
                    }
                }
            }
        }

        // Si no hi ha claus pendents, l'objectiu és la sortida
        if (objectius.isEmpty()) {
            objectius.add(estat.getSortidaPosicio());
        }

        int h = Integer.MAX_VALUE;
        
        // 2. Per cada agent, trobar la distància real a l'objectiu més proper
        for (Posicio agentPos : estat.getAgents()) {
            int distAgent = bfsDistanciaReal(estat, agentPos, objectius);
            h = Math.min(h, distAgent);
        }

        // Si per alguna raó un agent no pot arribar a cap objectiu
        // (p.ex. tancat per parets), retornem un valor alt
        return (h == Integer.MAX_VALUE) ? 1000 : h;
    }

    
    /**
     * Calcula la distància de camí real més curta des d'una posició 'start'
     * fins a qualsevol de les posicions 'targets', respectant parets
     * i portes que l'estat actual permet obrir.
     * * @param estat L'estat actual (per saber quines portes podem obrir)
     * @param start La posició inicial (p.ex. un agent)
     * @param targets El conjunt de posicions objectiu
     * @return La distància (cost) més curta, o Integer.MAX_VALUE si és impossible
     */
    private int bfsDistanciaReal(Mapa estat, Posicio start, Collection<Posicio> targets) {
        
        // Cua pel BFS
        Queue<Posicio> queue = new LinkedList<>();
        
        // Mapa per desar la distància (cost) a cada posició
        Map<Posicio, Integer> dist = new HashMap<>();

        queue.add(start);
        dist.put(start, 0);

        while (!queue.isEmpty()) {
            Posicio actual = queue.poll();
            int costActual = dist.get(actual);

            // 1. Hem trobat un objectiu?
            if (targets.contains(actual)) {
                return costActual; // Retornem el cost per arribar-hi
            }

            // 2. Expandir veïns (Amunt, Avall, Esquerra, Dreta)
            for (Direccio d : Direccio.values()) {
                Posicio vei = actual.translate(d);
                int cell = estat.getCell(vei.x, vei.y);

                // Comprovar si ja l'hem visitat
                if (dist.containsKey(vei)) {
                    continue;
                }

                // Comprovar obstacles
                if (cell == Mapa.PARET) {
                    continue;
                }
                
                // Comprovar portes
                if (Character.isUpperCase(cell)) {
                    if (!estat.portaObrible((char) cell)) {
                        continue; // Porta tancada
                    }
                }
                
                // Si és un moviment vàlid, l'afegim
                dist.put(vei, costActual + 1);
                queue.add(vei);
            }
        }
        
        // No s'ha trobat cap camí
        return Integer.MAX_VALUE;
    }
}