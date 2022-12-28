function generateGrid(gameId) {
    console.log("Inside js method... ")
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

function toggle(pos, gameId) {
    console.log("Clicked on the cell: "+pos)
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
function createUser() {
    console.log("before extracting.. ")
    let u = document.getElementById("create_username").value
    let p = document.getElementById("create_password").value

    var json = {}
    json['userId'] = 0
    json['username'] = u
    json['password'] = p
    json['active'] = true
    console.log(json)

    const saveUserOp = async ()  => {
        const res = await makeRequest('api/users', 'POST', JSON.stringify(json))
        console.log('response is ')
        console.log(res)
        console.log(res.status)
        let lbl = document.getElementById("message")
        if(res.status === 200) {
            lbl.style.color = 'green'
            lbl.innerHTML = "Successfully saved user"
        } else {
            lbl.style.color = 'red'
            lbl.innerHTML = "Failed to save, contact administrator for more details"
        }
        var myJson = await res.json()
        console.log(myJson)
    }
    saveUserOp()
}