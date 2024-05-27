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

// Obtener el ID del usuario de la solicitud GET
$usuarioId = $_GET['usuario_id'];

// Consulta SQL para obtener los IDs de las historias favoritas del usuario
$sql = "SELECT HistoriaID FROM favoritosUsuario WHERE UsuarioID = ?";

// Preparar la consulta
$stmt = $conn->prepare($sql);

// Validar si la preparación de la consulta fue exitosa
if (!$stmt) {
    error_log("Error en la preparación de la consulta: " . $conn->error);
    die(json_encode(array('status' => 'Error', 'message' => 'Error en la preparación de la consulta')));
}

// Vincular el parámetro del usuario a la consulta preparada
$stmt->bind_param("i", $usuarioId);

// Ejecutar la consulta
$stmt->execute();

// Obtener el resultado
$result = $stmt->get_result();

// Crear un array para almacenar los IDs de las historias favoritas
$historiasFavoritasIds = array();

// Recorrer los resultados y agregar los IDs al array
while ($row = $result->fetch_assoc()) {
    $historiasFavoritasIds[] = $row['HistoriaID'];
}

// Devolver los IDs de las historias favoritas en formato JSON
echo json_encode($historiasFavoritasIds);

// Cerrar la consulta y la conexión
$stmt->close();
$conn->close();
?>
