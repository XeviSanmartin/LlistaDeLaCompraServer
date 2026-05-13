package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.websockects.EsdevenimentLlista
import cat.montilivi.lallistadelacompra.model.requests.PeticioActualitzacioProducteDeLaLlista
import cat.montilivi.lallistadelacompra.model.requests.PeticioProducteDeLaLlista
import cat.montilivi.lallistadelacompra.model.websockects.TipusAccio
import cat.montilivi.lallistadelacompra.model.eines.toCampActualitzable
import cat.montilivi.lallistadelacompra.repositori.GestorDeConnexions
import cat.montilivi.lallistadelacompra.repositori.RepositoriLlistesDeLaCompra
import cat.montilivi.lallistadelacompra.repositori.RepositoriProducteDeLaLlista
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route


fun Route.rutesDelsProductesDUnaLlista() {
    route("/llistes/{idLlista}/productes") {
        get {
            val idLlista = call.parameters["idLlista"]?.toIntOrNull()
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()

            if (idUsuari != null) {
                if (idLlista != null && RepositoriLlistesDeLaCompra.existeixLlista(idLlista)) {
                    val llista = RepositoriLlistesDeLaCompra.cercaLlistaPerId(idLlista)
                    if (llista?.idsPropietaris?.contains(idUsuari) == true) {
                        val productes = RepositoriProducteDeLaLlista.cercaProductesPerLlista(idLlista)
                        call.respond(productes)
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            "No tens permís per veure els productes d'aquesta llista"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Llista no trobada")
                }
            }
        }

        post ()
        {
            val idLlista = call.parameters["idLlista"]?.toIntOrNull()
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()
            val request = call.receive<PeticioProducteDeLaLlista>()

            if (idUsuari != null) {
                if (idLlista != null && RepositoriLlistesDeLaCompra.existeixLlista(idLlista)) {
                    val llista = RepositoriLlistesDeLaCompra.cercaLlistaPerId(idLlista)
                    if (llista?.idsPropietaris?.contains(idUsuari) == true) {
                        if (!RepositoriProducteDeLaLlista.existeixProductePerId(
                                idLlista,
                                idProducte = request.idProducte
                            )) {
                            val producteNou = RepositoriProducteDeLaLlista.creaProducte(
                                idLlista,
                                request.idProducte,
                                request.quantitat ,
                                request.unitat,
                                request.estaComprat,
                                request.quiHaComprat
                            )
                            if (producteNou!= null)
                            {
//                                // --- CODI WEBSOCKET ---
//                                // Busquem qui ha de saber-ho
//                                val idsAVisualitzar = RepositoriLlistesDeLaCompra.obtenIdsPropietaris(producteNou.idLLista)
//
//                                // Enviem l'esdeveniment
//                                GestorDeConnexions.enviaAUsuarisConcrets(
//                                    idsAVisualitzar,
//                                    EsdevenimentLlista(TipusAccio.PRODUCTE_AFEGIT, producteNou.idLLista, producteNou.idProducte, producteNou)
//                                )
//                                // ----------------------
                                GestorDeConnexions.notificaAccio(TipusAccio.PRODUCTE_AFEGIT, producteNou.idLLista, producteNou.idProducte, producteNou)
                                call.respond(HttpStatusCode.Created, "Producte afegit a la llista amb ID: ${producteNou.idProducte}")
                            }
                            else
                                call.respond(HttpStatusCode.InternalServerError, "No s'ha pogut crear el producte")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "El producte especificat ja existeix")
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            "No tens permís per veure els productes d'aquesta llista"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Llista no trobada")
                }
            }
        }
        patch("/{idProducte}") {
            val idLlista = call.parameters["idLlista"]?.toIntOrNull()
            val idProducte = call.parameters["idProducte"]?.toIntOrNull()
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()
            val request = call.receive<PeticioActualitzacioProducteDeLaLlista>()

            if (idUsuari != null) {
                if (idLlista != null&& RepositoriLlistesDeLaCompra.existeixLlista(idLlista)) {
                    val llista = RepositoriLlistesDeLaCompra.cercaLlistaPerId(idLlista)
                    if (llista?.idsPropietaris?.contains(idUsuari) == true) {
                        if (idProducte != null && RepositoriProducteDeLaLlista.existeixProductePerId( idLlista, idProducte)) {
                            val resultat = RepositoriProducteDeLaLlista.actualitzaProducte(
                                idLlista,
                                idProducte,
                                request.quantitat.toCampActualitzable(),
                                request.unitat.toCampActualitzable(),
                                request.estaComprat.toCampActualitzable(),
                                request.quiHaComprat.toCampActualitzable()
                            )
                            if (resultat) {
                                // --- CODI WEBSOCKET ---
                                // Busquem qui ha de saber-ho
                                val producteActualitzat = RepositoriProducteDeLaLlista.cercaProductePerId(idLlista, idProducte)
                                require(producteActualitzat != null)
                                val idsAVisualitzar = RepositoriLlistesDeLaCompra.obtenIdsPropietaris(producteActualitzat.idLLista)

                                // Enviem l'esdeveniment
                                GestorDeConnexions.enviaAUsuarisConcrets(
                                    idsAVisualitzar,
                                    EsdevenimentLlista(TipusAccio.PRODUCTE_ACTUALITZAT, producteActualitzat.idLLista, producteActualitzat.idProducte, producteActualitzat)
                                )
                                // ----------------------
                                call.respond(HttpStatusCode.OK, "Producte actualitzat correctament")
                            }
                            else
                                call.respond(HttpStatusCode.InternalServerError, "No s'ha pogut actualitzar el producte")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "El producte especificat no existeix")
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            "No tens permís per veure els productes d'aquesta llista"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Llista no trobada")
                }
            }
        }

        delete("/{idProducte}") {
            val idLlista = call.parameters["idLlista"]?.toIntOrNull()
            val idProducte = call.parameters["idProducte"]?.toIntOrNull()
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()

            if (idUsuari != null) {
                if (idLlista != null&& RepositoriLlistesDeLaCompra.existeixLlista(idLlista)) {
                    val llista = RepositoriLlistesDeLaCompra.cercaLlistaPerId(idLlista)
                    if (llista?.idsPropietaris?.contains(idUsuari) == true) {
                        if (idProducte != null && RepositoriProducteDeLaLlista.existeixProductePerId( idLlista, idProducte)) {
                            val resultat = RepositoriProducteDeLaLlista.eliminaProducte(
                                idLlista,
                                idProducte
                            )
                            if(resultat) {
//                                // --- CODI WEBSOCKET ---
//                                // Busquem qui ha de saber-ho
//                                val idsAVisualitzar = RepositoriLlistesDeLaCompra.obtenIdsPropietaris(idLlista)
//
//                                // Enviem l'esdeveniment
//                                GestorDeConnexions.enviaAUsuarisConcrets(
//                                    idsAVisualitzar,
//                                    EsdevenimentLlista(TipusAccio.PRODUCTE_ELIMINAT, idLlista, idProducte, null)
//                                )
//                                // ----------------------
                                GestorDeConnexions.notificaAccio(TipusAccio.PRODUCTE_ELIMINAT, idLlista, idProducte, null)
                                call.respond(HttpStatusCode.OK, "El producte s'ha eliminat correctament")
                            }
                            else
                                call.respond(HttpStatusCode.InternalServerError, "No s'ha pogut eliminar el producte")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "El producte especificat no existeix")
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            "No tens permís per veure els productes d'aquesta llista"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Llista no trobada")
                }
            }
        }
    }
}