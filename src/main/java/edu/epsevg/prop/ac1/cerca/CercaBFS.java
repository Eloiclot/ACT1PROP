package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaBFS extends Cerca {
    public CercaBFS(boolean usarLNT) { super(usarLNT); }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        
        // Llista de Nodes Oberts (LNO) - Frontera (BFS usa una Cola)
        Queue<Node> lno = new LinkedList<>();

        // Llista de Nodes Tancats (LNT) - Per control de cicles
        // Guardem el mapa i la profunditat a la que es va visitar
        Map<Mapa, Integer> lnt = new HashMap<>();

        // 1. Crear el node inicial
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        lno.add(nodeInicial);

        if (usarLNT) {
            lnt.put(inicial, 0);
        }

        // 2. Bucle principal de cerca
        while (!lno.isEmpty()) {
            
            // 3. Extreure el següent node de la LNO
            Node actual = lno.poll();
            rc.incNodesExplorats(); // Comptem el node com a explorat
            
            // 4. Comprovar si és meta
            if (actual.estat.esMeta()) {
                // Solució trobada! Reconstruïm el camí
                rc.setCami(reconstruirCami(actual));
                return; // Acabem la cerca
            }

            // 5. Generar successors (fills)
            for (Moviment accio : actual.estat.getAccionsPossibles()) {
                try {
                    Mapa nouEstat = actual.estat.mou(accio);
                    Node nouNode = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

                    // 6. Control de cicles
                    if (usarLNT) {
                        // Mirem si ja hem visitat aquest estat
                        Integer profundaVisitada = lnt.get(nouEstat);
                        
                        if (profundaVisitada != null && nouNode.depth >= profundaVisitada) {
                            // Ja l'hem visitat a una profunditat menor o igual
                            rc.incNodesTallats();
                            continue; // Descartem el node
                        }
                        // Si no l'hem visitat, o el visitem amb menys profunditat, l'afegim/actualitzem
                        lnt.put(nouEstat, nouNode.depth);
                        
                    } else {
                        // Control de cicles dins la branca actual
                        if (estaEnCami(nouNode)) {
                            rc.incNodesTallats();
                            continue; // Descartem el node
                        }
                    }

                    // 7. Afegir el nou node a la LNO
                    lno.add(nouNode);

                } catch (IllegalArgumentException e) {
                    // Moviment invàlid (p.ex. col·lisió o porta tancada)
                    // No fem res, simplement no generem aquest successor
                }
            }
            
            // 8. Actualitzar memòria pic
            int memoriaActual = lno.size() + (usarLNT ? lnt.size() : 0);
            rc.updateMemoria(memoriaActual);
        }
        
        // Si sortim del bucle sense return, no s'ha trobat solució
        // rc.getCami() ja és null per defecte
    }
    
    
    /**
     * Comprova si el nou node genera un cicle dins la seva pròpia branca.
     * Puja recursivament cap als pares
     * @param node El node a comprovar
     * @return true si el seu estat ja existeix en un dels seus avantpassats.
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
     * @param nodeFinal El node que ha assolit l'estat meta.
     * @return Llista de moviments.
     */
    private List<Moviment> reconstruirCami(Node nodeFinal) {
        LinkedList<Moviment> cami = new LinkedList<>();
        Node actual = nodeFinal;
        while (actual.pare != null) { // Mentre no arribem a l'arrel (que no té acció)
            cami.addFirst(actual.accio);
            actual = actual.pare;
        }
        return cami;
    }
}