package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

import java.util.ArrayList;
import java.util.List;

/** * Distància de Manhattan a la clau més propera 
 * (si queden per recollir) o a la sortida.
 */
public class HeuristicaBasica implements Heuristica {
    @Override
    public int h(Mapa estat) {
        
        //@TODO: reemplaceu tot el codi per la vostra heurística.
        
        if(estat.esMeta()) {
            return 0;
        }

        List<Posicio> clausPendents = new ArrayList<>();
        
        // 1. Buscar totes les claus pendents al mapa
        for (int i = 0; i < estat.getN(); i++) {
            for (int j = 0; j < estat.getM(); j++) {
                int cell = estat.getCell(i, j);
                if (Character.isLowerCase(cell)) {
                    // És una clau. Comprovem si JA la tenim.
                    if (!estat.teClau((char) cell)) {
                        clausPendents.add(new Posicio(i, j));
                    }
                }
            }
        }

        int h = Integer.MAX_VALUE;
        List<Posicio> agents = estat.getAgents();

        if (!clausPendents.isEmpty()) {
            // 2. Si queden claus: Dist. Manhattan a la clau més propera [cite: 65]
            for (Posicio agent : agents) {
                for (Posicio clau : clausPendents) {
                    int dist = Math.abs(agent.x - clau.x) + Math.abs(agent.y - clau.y);
                    h = Math.min(h, dist);
                }
            }
        } else {
            // 3. Si NO queden claus: Dist. Manhattan a la sortida [cite: 66]
            Posicio sortida = estat.getSortidaPosicio();
            for (Posicio agent : agents) {
                int dist = Math.abs(agent.x - sortida.x) + Math.abs(agent.y - sortida.y);
                h = Math.min(h, dist);
            }
        }

        return h;
    }
}