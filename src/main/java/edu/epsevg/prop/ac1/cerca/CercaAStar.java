package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.heuristica.Heuristica;
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaAStar extends Cerca {

    private final Heuristica heur;
    
    // Cache per desar els valors h(mapa) i no recalcular-los
    private final Map<Mapa, Integer> hCache = new HashMap<>();

    public CercaAStar(boolean usarLNT, Heuristica heur) { 
        super(usarLNT); 
        this.heur = heur; 
    }

    /**
     * Mètode privat que obté la 'h' d'un estat, usant la cache si
     * ja s'havia calculat abans.
     */
    private int getH(Mapa estat) {
        return hCache.computeIfAbsent(estat, e -> heur.h(e));
    }

    @Override
    public  void ferCerca(Mapa inicial, ResultatCerca rc) {
         
        // Llista de Nodes Oberts (LNO) - Frontera (A* usa una Cola de Prioritat)
        // La prioritat és f(n) = g(n) + h(n)
        Comparator<Node> nodeComparator = Comparator.comparingInt(n -> n.g + getH(n.estat));
        PriorityQueue<Node> lno = new PriorityQueue<>(nodeComparator);

        // Llista de Nodes Tancats (LNT)
        // Desem el mapa i el COST (g) mínim conegut per arribar-hi.
        // En aquest problema, g == depth, complint el requisit de l'enunciat.
        Map<Mapa, Integer> lnt = new HashMap<>();

        // 1. Crear el node inicial
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        lno.add(nodeInicial);

        if (usarLNT) {
            lnt.put(inicial, 0); // Cost (g) per arribar a l'inici és 0
        }

        // 2. Bucle principal de cerca
        while (!lno.isEmpty()) {
            
            // 3. Extreure el següent node de la LNO (el de menor 'f')
            Node actual = lno.poll();
            rc.incNodesExplorats();
            
            // 4. Comprovar si és meta
            if (actual.estat.esMeta()) {
                rc.setCami(reconstruirCami(actual));
                return;
            }

            // 5. Generar successors (fills)
            for (Moviment accio : actual.estat.getAccionsPossibles()) {
                try {
                    Mapa nouEstat = actual.estat.mou(accio);
                    // El cost 'g' del fill és el del pare + 1
                    int nou_g = actual.g + 1;
                    Node nouNode = new Node(nouEstat, actual, accio, actual.depth + 1, nou_g);

                    // 6. Control de cicles
                    if (usarLNT) {
                        Integer costVisitat = lnt.get(nouEstat);
                        
                        // Si ja hem visitat aquest estat (costVisitat != null)
                        // i el camí que havíem trobat era pitjor o igual (nou_g >= costVisitat)
                        // Llavors tallem (pruning).
                        if (costVisitat != null && nou_g >= costVisitat) {
                            rc.incNodesTallats();
                            continue; 
                        }
                        // Si és un estat nou, o hem trobat un camí MILLOR (més barat),
                        // l'afegim/actualitzem a la LNT i a la LNO.
                        lnt.put(nouEstat, nou_g);
                        
                    } else {
                        // Control de cicles dins la branca actual
                        if (estaEnCami(nouNode)) {
                            rc.incNodesTallats();
                            continue;
                        }
                    }

                    // 7. Afegir el nou node a la LNO
                    lno.add(nouNode);

                } catch (IllegalArgumentException e) {
                    // Moviment invàlid
                }
            }
            
            // 8. Actualitzar memòria pic
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