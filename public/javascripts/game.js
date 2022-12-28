function generateGrid(gameId) {
    let tbl = document.getElementById("card_holder")
    for(let i = 0; i < 25; i++) {
        let cell = document.createElement("div");
        cell.id = "row-"+i;
        cell.className = "card";
        cell.innerHTML = i;
        cell.setAttribute("onclick","toggle("+i+","+gameId+");");
        cell.classList.add('tooltip')
        tbl.appendChild(cell);
    }
}

function initGame(gameId) {
    const url = 'api/games/'+gameId+'/initialize'
    const initializeGameRequest = async ()  => {
        const res = await makeRequest(url, 'GET', undefined)
        let el = document.getElementById("err_msg")
        if(res.status === 200) {
            el.innerHTML = "Game Initialized Successfully"
            el.style.color = 'green'
        } else {
            el.innerHTML = "Game initialization failed"
            el.style.color = 'red'
        }
    }
    initializeGameRequest()
}

function deleteProgress(gameId) {
    const url = 'api/games/'+gameId+'/deleteProgress'
    const invoke = async ()  => {
        const res = await makeRequest(url, 'DELETE', undefined)
        let el = document.getElementById("err_msg")
        if(res.status === 200) {
            el.innerHTML = "Game progress deleted successfully"
            el.style.color = 'green'
        } else {
            let msg = await res.text()
            el.innerHTML = "Could not delete the game progress. Reason: "+msg
            el.style.color = 'red'
        }
    }
    invoke()
}

function toggle(pos, gameId) {
    console.log("Clicked on the cell: "+pos)
    let cell = document.getElementById("row-"+pos)
    cell.style = "box-shadow: 10px 10px darkblue;"
    const claimEgg = async () => {
        const response = await fetch('http://localhost:9000/api/games/'+gameId+'/reveal/'+pos, {
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
        if(res.status === 200) {
            let value = await res.json()
            if(value.alreadyClaimed) {
                el.innerHTML = "Found an egg, but it is already claimed by "+value.user.username
                el.style.color = 'Tomato'
            } else {
                el.innerHTML = "Yay, you found the hidden egg and claimed it! Congrats!"
                el.style.color = 'green'
            }
        } else {
            el.innerHTML = "No Egg here, better luck next time"
            el.style.color = 'red'
        }
    }
    claimEgg()
}


function makeRequest(path, met, obj) {
    const url = 'http://localhost:9000/'+path
    console.log("JSON = "+obj)
    const response =  fetch(url, {
        method: met,
        body: obj, // string or object
        headers: {
            'Content-Type': 'application/json',
        }
    });
    return response;
}