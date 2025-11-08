package edu.epsevg.prop.ac1.cerca;
 
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaDFS extends Cerca {
    
    // Límit de profunditat per DFS 
    private static final int MAX_DEPTH_LIMIT = 50;
    
    public CercaDFS(boolean usarLNT) { super(usarLNT); }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
     
        // Llista de Nodes Oberts (LNO) - Frontera (DFS usa una Pila)
        Stack<Node> lno = new Stack<>();

        // Llista de Nodes Tancats (LNT)
        Map<Mapa, Integer> lnt = new HashMap<>();

        // 1. Crear el node inicial
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        lno.push(nodeInicial); // S'afegeix amb push

        if (usarLNT) {
            lnt.put(inicial, 0);
        }

        // 2. Bucle principal de cerca
        while (!lno.isEmpty()) {
            
            // 3. Extreure el següent node de la LNO
            Node actual = lno.pop(); // S'extreu amb pop
            rc.incNodesExplorats();
            
            // 4. Comprovar si és meta
            if (actual.estat.esMeta()) {
                rc.setCami(reconstruirCami(actual));
                return;
            }

            // 5. CONTROL DE LÍMIT DE PROFUNDITAT 
            if (actual.depth >= MAX_DEPTH_LIMIT) {
                rc.incNodesTallats(); // Tallem per profunditat
                continue; // No explorem més enllà d'aquest node
            }

            // 6. Generar successors (fills)
            // NOTA: Per replicar el comportament d'un DFS recursiu (provar AMUNT, AVALL, ...)
            // hauríem d'invertir l'ordre d'inserció a la pila. 
            // Però per una cerca DFS estàndard, l'ordre de getAccionsPossibles ja és bo.
            for (Moviment accio : actual.estat.getAccionsPossibles()) {
                try {
                    Mapa nouEstat = actual.estat.mou(accio);
                    Node nouNode = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

                    // 7. Control de cicles
                    if (usarLNT) {
                        Integer profundaVisitada = lnt.get(nouEstat);
                        
                        if (profundaVisitada != null && nouNode.depth >= profundaVisitada) {
                            rc.incNodesTallats();
                            continue;
                        }
                        lnt.put(nouEstat, nouNode.depth);
                        
                    } else {
                        if (estaEnCami(nouNode)) {
                            rc.incNodesTallats();
                            continue;
                        }
                    }

                    // 8. Afegir el nou node a la LNO
                    lno.push(nouNode); // S'afegeix amb push

                } catch (IllegalArgumentException e) {
                    // Moviment invàlid
                }
            }
            
            // 9. Actualitzar memòria pic
            int memoriaActual = lno.size() + (usarLNT ? lnt.size() : 0);
            rc.updateMemoria(memoriaActual);
        }
    }
    
    
    /**
     * Comprova si el nou node genera un cicle dins la seva pròpia branca.
     */
    private boolean estaEnCami(Node node) {
        Node pare = node.pare;
        while (pare != null) {
            if (node.estat.equals(pare.estat)) {
                return true;
            }
            pare = pare.pare;
        }
        return false;
    }

    /**
     * Reconstrueix la llista de moviments des del node solució fins al node arrel.
     */
    private List<Moviment> reconstruirCami(Node nodeFinal) {
        LinkedList<Moviment> cami = new LinkedList<>();
        Node actual = nodeFinal;
        while (actual.pare != null) {
            cami.addFirst(actual.accio);
            actual = actual.pare;
        }
        return cami;
    }
}