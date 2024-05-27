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

// Obtener datos de la solicitud GET
$historiaId = $_GET['historia_id'];
$usuarioId = $_GET['usuario_id'];

// Verificar si la historia ya está guardada como favorita
$sqlCheck = "SELECT * FROM favoritosUsuario WHERE UsuarioID = ? AND HistoriaID = ?";
$stmtCheck = $conn->prepare($sqlCheck);
$stmtCheck->bind_param("ii", $usuarioId, $historiaId);
$stmtCheck->execute();
$resultCheck = $stmtCheck->get_result();

if ($resultCheck->num_rows > 0) {
    // La historia ya está guardada como favorita
    echo json_encode(array('status' => 'Error', 'message' => 'Ya has guardado esta historia'));
} else {
    // La historia no está guardada, proceder con la inserción
    $stmt = $conn->prepare("INSERT INTO favoritosUsuario (UsuarioID, HistoriaID) VALUES (?, ?)");
    $stmt->bind_param("ii", $usuarioId, $historiaId);

    if ($stmt->execute()) {
        echo json_encode(array('status' => 'Success', 'message' => 'Favorito actualizado'));
    } else {
        echo json_encode(array('status' => 'Error', 'message' => 'Error al actualizar favorito: ' . $stmt->error));
    }
    $stmt->close();
}

// Cerrar la consulta y la conexión
$stmtCheck->close();
$conn->close();
?>
