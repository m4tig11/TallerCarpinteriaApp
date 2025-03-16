package com.example.tallercarpinteria.api

data class Pedido(
    val id: Int,
    val cliente_nombre: String,
    val estado: String,
    val fecha_medicion: String,
    val fecha_presupuesto: String? = null,
    val fecha_materiales: String? = null,
    val fecha_entrega: String? = null,
    val imagen_url: String? = null,
    val notas: String? = null,
    val presupuesto: Double? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val detalles_medicion: String? = null,
    val lista_materiales: String? = null,
    val observaciones: String? = null,
    val plano: String? = null
) {
    override fun toString(): String {

        return """
            Pedido(
                id=$id,
                cliente=$cliente_nombre,
                estado=$estado,
                fecha_medicion=$fecha_medicion,
                fecha_presupuesto=$fecha_presupuesto,
                fecha_materiales=$fecha_materiales,
                fecha_entrega=$fecha_entrega,
                imagen_url=$imagen_url,
                notas=$notas,
                presupuesto=$presupuesto,
                direccion=$direccion,
                telefono=$telefono,
                email=$email,
                detalles_medicion=$detalles_medicion,
                lista_materiales=$lista_materiales,
                observaciones=$observaciones
                plano=$plano
            )
        """.trimIndent()
    }
} 