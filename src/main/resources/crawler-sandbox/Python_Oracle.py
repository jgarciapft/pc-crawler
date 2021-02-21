import cx_Oracle

# PRÁCTICA 02b
# Autor: Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)

def dbConectar():
    ip = "localhost"
    puerto = 1521
    sid = "xe"

    print("---dbConectar---")
    url = cx_Oracle.makedsn(ip, puerto, sid)

    usuario = "system"
    contrasena = "12345"

    print("---Conectando a Oracle---")

    try:
        conexion = cx_Oracle.connect(usuario, contrasena, url)
        print("Conexión realizada a la base de datos", conexion)
        print(conexion.version)
        return conexion
    except cx_Oracle.falloor as fallo:
        print("fallo en la conexión")
        print(fallo)
        return None


# ------------------------------------------------------------------

def dbDesconectar():
    print("---dbDesconectar---")
    try:
        conexion.commit()
        conexion.close()
        print("Desconexión realizada correctamente")
        return True
    except cx_Oracle.falloor as fallo:
        print("fallo en la desconexión")
        print(fallo)
        return False

# ------------------------------------------------------------------
# ------------------------------------------------------------------
# ------------------------------------------------------------------

print("---Programa principal---")

conexion = dbConectar()

if conexion is None:
    print("FALLO DE CONEXIÓN")
else:
    print("CONEXIÓN REALIZADA")

    dbMostrarEmpleados1()
    dbMostrarEmpleados2()
    dbMostrarEmpleados3()
    dbMostrarEmpleados4()
    dbMostrarEmpleados5()

    dbObtenerEmpleados()  # Probar con el DNI 987654321
    dbConsultarEmpleados()
    dbConsultarDepartamentos()

    dbInsertarDepartamentos()  # Probar con: { nombreDpto = INVESTIGACIÓN, numeroDpto = 5 }
    dbConsultarDepartamentos()

    dbModificarDepartamentos()  # Probar a modificar el departamento 5 / nuevo nombre: DESCONOCIDO
    dbConsultarDepartamentos()

    dbBorrarDepartamentos()  # Probar a borrar el departamento 5
    dbConsultarDepartamentos()

    dbInsertarMultiplesDepartamentos()
    dbConsultarDepartamentos()

    dbBorrarMultiplesDepartamentos()
    dbConsultarDepartamentos()

    dbDesconectar()

print("---Fin de programa---")
