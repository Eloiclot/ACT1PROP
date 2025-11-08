package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaIDS extends Cerca {
    public CercaIDS(boolean usarLNT) { super(usarLNT); }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        
        int nodesExploratsTotals = 0; // Acumulador de nodes
        int nodesTallatsTotals = 0;
        int memoriaPicMaxima = 0;

        // 1. Bucle de profunditat iterativa
        for (int limitProfunditat = 0; limitProfunditat < Integer.MAX_VALUE; limitProfunditat++) {
            
            // Llista de Nodes Oberts (LNO) - Frontera (DFS usa una Pila)
            Stack<Node> lno = new Stack<>();

            // Llista de Nodes Tancats (LNT)
            // Es reinicia CADA iteració de profunditat
            Map<Mapa, Integer> lnt = new HashMap<>();

            // 2. Crear el node inicial
            Node nodeInicial = new Node(inicial, null, null, 0, 0);
            lno.push(nodeInicial);

            if (usarLNT) {
                lnt.put(inicial, 0);
            }
            
            int nodesExploratsIteracio = 0;
            int nodesTallatsIteracio = 0;
            int memoriaPicIteracio = 0;

            // 3. Bucle principal de cerca (DFS limitat)
            while (!lno.isEmpty()) {
                
                // 4. Extreure el següent node de la LNO
                Node actual = lno.pop();
                nodesExploratsIteracio++;
                
                // 5. Comprovar si és meta
                if (actual.estat.esMeta()) {
                    // Solució trobada!
                    // Actualitzem els totals al ResultatCerca
                    rc.incNodesExplorats(); // Comptem l'últim node explorat (la meta)
                    rc.updateMemoria(memoriaPicMaxima);
                    rc.setCami(reconstruirCami(actual));
                    return; // Acabem la cerca
                }

                // 6. CONTROL DE LÍMIT DE PROFUNDITAT
                if (actual.depth >= limitProfunditat) {
                    nodesTallatsIteracio++;
                    continue; // No explorem més enllà d'aquest node
                }

                // 7. Generar successors (fills)
                for (Moviment accio : actual.estat.getAccionsPossibles()) {
                    try {
                        Mapa nouEstat = actual.estat.mou(accio);
                        Node nouNode = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

                        // 8. Control de cicles
                        if (usarLNT) {
                            Integer profundaVisitada = lnt.get(nouEstat);
                            
                            if (profundaVisitada != null && nouNode.depth >= profundaVisitada) {
                                nodesTallatsIteracio++;
                                continue;
                            }
                            lnt.put(nouEstat, nouNode.depth);
                            
                        } else {
                            if (estaEnCami(nouNode)) {
                                nodesTallatsIteracio++;
                                continue;
                            }
                        }

                        // 9. Afegir el nou node a la LNO
                        lno.push(nouNode);

                    } catch (IllegalArgumentException e) {
                        // Moviment invàlid
                    }
                }
                
                // 10. Actualitzar memòria pic d'aquesta iteració
                int memoriaActual = lno.size() + (usarLNT ? lnt.size() : 0);
                if (memoriaActual > memoriaPicIteracio) {
                    memoriaPicIteracio = memoriaActual;
                }
            } // Fi bucle DFS

            // 11. Acumular resultats de la iteració (com demana l'enunciat) 
            for (int i = 0; i < nodesExploratsIteracio; i++) rc.incNodesExplorats();
            for (int i = 0; i < nodesTallatsIteracio; i++) rc.incNodesTallats();
            
            if (memoriaPicIteracio > memoriaPicMaxima) {
                memoriaPicMaxima = memoriaPicIteracio;
            }
            rc.updateMemoria(memoriaPicMaxima); // Actualitzem la memòria pic global
            
            // Si la LNO s'ha buidat i no hem trobat solució (i no hem tallat res),
            // vol dir que hem explorat tot l'arbre i no hi ha solució.
            if (nodesExploratsIteracio == 0 && nodesTallatsIteracio == 0) {
                 // No s'ha trobat solució en cap profunditat
                 return;
            }
        } // Fi bucle IDS
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