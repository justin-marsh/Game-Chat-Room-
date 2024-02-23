let ws;
function newRoom(){
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })

        .then(response => response.text())
        .then(response => {
            enterRoom(response)
            const roomDiv = document.getElementById("room");
            const codeElement = document.createElement("div");

            codeElement.textContent =  response;
            codeElement.classList.add("room-code"); // add a class for styling
            // add onclick event listener to call enterRoom() function with the text content of the div
            codeElement.onclick = function() {
                enterRoom(codeElement.textContent);
            };
            roomDiv.insertBefore(codeElement, roomDiv.firstChild); // insert above the button
             }); // enter the room with the code
}
function enterRoom(code){

    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/"+ code);

    // refresh the list of rooms
    refreshRoomList();
    //clear log from before
    document.getElementById("log").value = "";

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message
        document.getElementById("log").value +=  "[" + timestamp() + "] " + "[" + message.type + "] " + message.message + "\n";

        }

}


document.getElementById("input").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        let request = {"type":"chat", "msg":event.target.value};
        ws.send(JSON.stringify(request));
        event.target.value = "";
    }
});


function refreshRoomList() {
    const roomDiv = document.getElementById("room");
// remove existing room code elements
    while (roomDiv.childNodes.length > 2) {
        roomDiv.removeChild(roomDiv.firstChild);
    }
    // calling the ChatServlet to retrieve the list of available rooms
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
        },
    })
        .then(response => response.json())
        .then(data => {
            const roomList = data.rooms;
            const roomDiv = document.getElementById("room");
            // remove existing room code elements
            while (roomDiv.firstChild) {
                roomDiv.removeChild(roomDiv.firstChild);
            }
            // create new room code elements
            for (const roomCode of roomList) {
                const codeElement = document.createElement("div");
                codeElement.textContent = roomCode;
                codeElement.classList.add("room-code"); // add a class for styling
                // add onclick event listener to call enterRoom() function with the text content of the div
                codeElement.onclick = function() {
                    enterRoom(codeElement.textContent);
                };
                roomDiv.appendChild(codeElement);
            }
        });
}

function timestamp() {
    var d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}