<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "1234";
$dbname = "Android";

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(array('status' => 'Error', 'message' => 'Error de conexión: ' . $conn->connect_error)));
}

// Obtener el ID de la historia de la solicitud GET
$historiaId = $_GET['historia_id'];

// Consulta SQL para obtener los detalles de la historia
$sql = "SELECT HistoriaID, Titulo, Descripcion, Imagen FROM Historias WHERE HistoriaID = ?";

// Preparar la consulta
$stmt = $conn->prepare($sql);

if (!$stmt) {
    error_log("Error en la preparación de la consulta: " . $conn->error);
    die(json_encode(array('status' => 'Error', 'message' => 'Error en la preparación de la consulta')));
}

// Vincular el parámetro a la consulta preparada
$stmt->bind_param("i", $historiaId);

// Ejecutar la consulta
$stmt->execute();

// Obtener el resultado
$result = $stmt->get_result();

// Crear un array para almacenar los datos de la historia
$historia = array();

// Si se encontró la historia, agregarla al array
if ($row = $result->fetch_assoc()) {
    $historia = array(
        'HistoriaID' => $row['HistoriaID'],
        'Titulo' => $row['Titulo'],
        'Descripcion' => $row['Descripcion'],
        'Imagen' => base64_encode($row['Imagen'])
    );
}

// Devolver los datos de la historia en formato JSON
echo json_encode($historia);

// Cerrar la consulta y la conexión
$stmt->close();
$conn->close();
?>
