<?php
header('Content-Type: application/json');

$servername = "localhost"; // Reemplaza con la dirección de tu servidor MySQL
$username = "root";  	 // Reemplaza con tu nombre de usuario de MySQL
$password = "1234";  	 // Reemplaza con tu contraseña de MySQL
$dbname = "Android";  	 // Nombre de tu base de datos

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(array('error' => 'Error de conexión: ' . $conn->connect_error)));
}

// Obtener el ID de la historia desde la solicitud GET
if (isset($_GET['id'])) {
    $historiaId = $_GET['id'];

    // Consulta SQL para obtener los detalles de la historia
    $sql = "SELECT HistoriaID, Titulo, Descripcion, Imagen, Latitud, Longitud FROM Historias WHERE HistoriaID = $historiaId";
    $result = $conn->query($sql);

    if ($result->num_rows == 1) {
   	 $row = $result->fetch_assoc();
   	 $imagenBase64 = base64_encode($row['Imagen']);

   	 // Devolver los detalles de la historia en formato JSON
   	 echo json_encode(array(
   		 'HistoriaID' => $row['HistoriaID'],
   		 'Titulo' => $row['Titulo'],
   		 'Descripcion' => $row['Descripcion'],
   		 'Imagen' => $imagenBase64,
   		 'Latitud' => $row['Latitud'],
   		 'Longitud' => $row['Longitud']
   	 ));
    } else {
   	 echo json_encode(array('error' => 'Historia no encontrada'));
    }
} else {
    echo json_encode(array('error' => 'Falta el ID de la historia'));
}

// Cerrar conexión
$conn->close();
?>