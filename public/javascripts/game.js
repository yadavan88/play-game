function generateGrid(gameId) {
    let tbl = document.getElementById("card_holder")
    for (let i = 0; i < 25; i++) {
        let cell = document.createElement("div");
        cell.id = "row-" + i;
        cell.className = "card";
        cell.innerHTML = i;
        cell.setAttribute("onclick", "toggle(" + i + "," + gameId + ");");
        cell.classList.add('tooltip')
        tbl.appendChild(cell);
    }
}

function initGame(gameId) {
    const url = 'api/games/' + gameId + '/initialize'
    const initializeGameRequest = async () => {
        const res = await makeRequest(url, 'GET', undefined)
        let el = document.getElementById("err_msg")
        if (res.status === 200) {
            el.innerHTML = "Game Initialized Successfully"
            el.style.color = 'green'
        } else {
            el.innerHTML = "Game initialization failed"
            el.style.color = 'red'
        }
    }
    initializeGameRequest()
}

function showUpvoteButton(gameId, pos, msg, currentUpvote) {
    let secMsgInfoDiv = document.createElement("div")
    secMsgInfoDiv.setAttribute("id", "sec-msg-box")
    secMsgInfoDiv.innerHTML = "Here is the secret message on this egg. Upvote it if you like it!"
    let secMsgDiv = document.createElement("div")
    secMsgDiv.innerHTML = '<span id="secret_msg" style="color: green">' + msg + '</span>'
    secMsgDiv.setAttribute("style", "padding: 5px")
    secMsgInfoDiv.appendChild(secMsgDiv)

    let countDiv = document.createElement("div")
    countDiv.setAttribute("style", "padding: 5px")
    let countLbl = document.createElement("label")
    countLbl.setAttribute("id", "upvote-count-lbl")
    countLbl.innerHTML = "Upvotes: "

    let countValue = document.createElement("label")
    countValue.setAttribute("id", "upvote-count-value")
    countValue.innerText = currentUpvote

    countDiv.appendChild(countLbl)
    countDiv.appendChild(countValue)
    secMsgInfoDiv.appendChild(countDiv)

    let buttonDiv = document.createElement("div")
    buttonDiv.setAttribute("style", "padding:5px")

    let button = document.createElement("button")
    let buttonFn = 'upvote(' + gameId + ',' + pos + ')'
    button.setAttribute("onclick", buttonFn)
    button.innerHTML = "Upvote"
    buttonDiv.appendChild(button)
    secMsgInfoDiv.appendChild(buttonDiv)

    let infoBoxEl = document.getElementById('info_box')
    infoBoxEl.appendChild(secMsgInfoDiv)
}

function deleteUpvoteBox() {
    let infoBox = document.getElementById("info_box")
    while (infoBox.firstChild) {
        infoBox.removeChild(infoBox.firstChild)
    }
    console.log('deleted the div...')
}

function upvote(gameId, pos) {
    console.log("clicked on upvote for position: " + pos)
    const invoke = async () => {
        const url = "api/upvote/" + gameId + "/" + pos
        const res = await makeRequest(url, 'POST', undefined)
        let el = document.getElementById("err_msg")
        if (res.status === 200) {
            el.innerHTML = "Upvoted Successfully"
            el.style.color = 'green'
            let lbl = document.getElementById("upvote-count-value")
            lbl.innerHTML = parseInt(lbl.innerText) + 1
        } else {
            let msg = await res.text()
            el.innerHTML = "Failed to upvote. Reason: " + msg
            el.style.color = 'red'
        }
    }
    invoke()
}

function writeMessage(gameId, pos) {
    let msgEl = document.getElementById("sec-msg-txt")
    let msg = {"msg": msgEl.value}

    const invoke = async () => {
        const url = "api/games/msg/" + gameId + "/" + pos
        const res = await makeRequest(url, 'POST', JSON.stringify(msg))
        let el = document.getElementById("err_msg")
        if (res.status === 200) {
            el.innerHTML = "Wrote the message successfully to your claimed egg"
            el.style.color = 'green'
        } else {
            let msg = await res.text()
            el.innerHTML = "Failed to write the message. Reason: " + msg
            el.style.color = 'red'
        }
    }
    invoke()
}

function deleteProgress(gameId) {
    const url = 'api/games/' + gameId + '/deleteProgress'
    const invoke = async () => {
        const res = await makeRequest(url, 'DELETE', undefined)
        let el = document.getElementById("err_msg")
        if (res.status === 200) {
            el.innerHTML = "Game progress deleted successfully"
            el.style.color = 'green'
        } else {
            let msg = await res.text()
            el.innerHTML = "Could not delete the game progress. Reason: " + msg
            el.style.color = 'red'
        }
    }
    invoke()
}

function toggle(pos, gameId) {
    console.log("Clicked on the cell: " + pos)
    deleteUpvoteBox()
    let cell = document.getElementById("row-" + pos)
    cell.style = "box-shadow: 10px 10px darkblue;"
    const claimEgg = async () => {
        const response = await fetch('http://localhost:9000/api/games/' + gameId + '/reveal/' + pos, {
            method: 'POST',
            body: "", // string or object
            headers: {
                'Content-Type': 'application/json',
                'sessionKey': 'uuid'
            }
        });
        const res = await response; //extract JSON from the http response
        let el = document.getElementById("err_msg")
        el.innerHTML = ""
        if (res.status === 200) {
            let value = await res.json()
            if (value.alreadyClaimed) {
                el.innerHTML = "Found an egg, but it is already claimed by " + value.user.username
                el.style.color = 'Tomato'
                showUpvoteButton(gameId, pos, value.message, value.upvotes)
            } else {
                let infoBox = document.getElementById("info_box")
                let msgTextBox = document.createElement("input")
                msgTextBox.setAttribute("id", "sec-msg-txt")
                msgTextBox.setAttribute("style", "padding: 5px")
                msgTextBox.setAttribute("type", "text")
                infoBox.appendChild(msgTextBox)


                let writeButton = document.createElement("button")
                writeButton.innerHTML = "Write Message"
                let buttonFn = "writeMessage(" + gameId + "," + pos + ")"
                writeButton.setAttribute("onclick", buttonFn)

                infoBox.appendChild(writeButton)

                el.innerHTML = "Yay, you found the hidden egg and claimed it! Congrats! Write your secret message"
                el.style.color = 'green'
            }
        } else {
            let responseMsg = await res
            el.innerHTML = await responseMsg.text()
            el.style.color = 'red'
        }
    }
    claimEgg()
}


function makeRequest(path, met, obj) {
    const url = 'http://localhost:9000/' + path
    console.log("JSON = " + obj)
    const response = fetch(url, {
        method: met,
        body: obj, // string or object
        headers: {
            'Content-Type': 'application/json',
        }
    });
    return response;
}