<?php
header('Content-Type: application/json');

$servername = "localhost"; // Reemplaza con la dirección de tu servidor MySQL
$username = "root";      // Reemplaza con tu nombre de usuario de MySQL
$password = "1234";      // Reemplaza con tu contraseña de MySQL
$dbname = "Android";      // Nombre de tu base de datos

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(array('status' => 'Error', 'message' => 'Error de conexión: ' . $conn->connect_error)));
}

// Obtener datos de la solicitud GET
$historiaId = $_GET['historia_id'];
$usuarioId = $_GET['usuario_id'];

// Preparar la consulta SQL (usando consultas preparadas para mayor seguridad)
$stmt = $conn->prepare("INSERT INTO favoritosUsuario (UsuarioID, HistoriaID) VALUES (?, ?)");

// Validar si la preparación de la consulta fue exitosa
if (!$stmt) {
    error_log("Error en la preparación de la consulta: " . $conn->error);
    die(json_encode(array('status' => 'Error', 'message' => 'Error en la preparación de la consulta')));
}

// Vincular parámetros a la consulta preparada
$stmt->bind_param("ii", $usuarioId, $historiaId);

// Ejecutar la consulta
if ($stmt->execute()) {
    echo json_encode(array('status' => 'Success', 'message' => 'Favorito actualizado'));
} else {
    echo json_encode(array('status' => 'Error', 'message' => 'Error al actualizar favorito: ' . $stmt->error));
}

// Cerrar la consulta y la conexión
$stmt->close();
$conn->close();
?>