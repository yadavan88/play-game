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
      const myJson = await response.json(); //extract JSON from the http response
      console.log(myJson);
      let info = document.getElementById("info")
      info.innerHTML = myJson;
      // do something with myJson
    }
    claimEgg()
}
