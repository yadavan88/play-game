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
        } else if(res.status === 409) {
            lbl.style.color = 'red'
            lbl.innerHTML = await res.text()
        }else {
            lbl.style.color = 'red'
            lbl.innerHTML = "Failed to save, contact administrator for more details"
        }
        var myJson = await res.json()
        console.log(myJson)
    }
    saveUserOp()
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