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
    die("Connection failed: " . $conn->connect_error);
}

// Recibe los datos JSON enviados desde la aplicación Android
$data = json_decode(file_get_contents("php://input"), true);

// Verifica si se recibieron datos válidos
if ($data !== null && isset($data['NombreUsuario']) && isset($data['Contrasena'])) {
    // Extrae los datos recibidos
    $NombreUsuario = $data['NombreUsuario'];
    $Contrasena = $data['Contrasena'];

    // Verifica los datos con la base de datos (incluyendo la consulta del ID)
    $sql = "SELECT UsuarioID FROM Usuarios WHERE NombreUsuario = '$NombreUsuario' AND Contrasena = '$Contrasena'";
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        // Si las credenciales son correctas, obtiene el ID del usuario y lo envía en la respuesta JSON
        $row = $result->fetch_assoc();
        $usuarioID = $row["UsuarioID"];
        $response = array("success" => true, "message" => "Inicio de sesión exitoso", "usuarioID" => $usuarioID);
        echo json_encode($response);
    } else {
        // Si las credenciales son incorrectas, envía una respuesta JSON de error
        $response = array("success" => false, "message" => "Nombre de usuario o contraseña incorrectos");
        echo json_encode($response);
    }
} else {
    // Si los datos no son válidos, envía una respuesta JSON de error
    $response = array("success" => false, "message" => "Error al recibir datos");
    echo json_encode($response);
}

$conn->close();
?>

