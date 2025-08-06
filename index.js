var express = require("express")
var bodyParser = require("body-parser")
var mongoose = require("mongoose")

const app = express()

app.use(bodyParser.json())
app.use(express.static('public'))
app.use(bodyParser.urlencoded({
    extended: true
}))



mongoose.connect('mongodb://0.0.0.0:27017/mydb', {
    useNewUrlParser: true,
    useUnifiedTopology: true
});
var db = mongoose.connection;

db.on('error', () => console.log("Error in Connecting to Database"));
db.once('open', () => console.log("Connected to Database"));



const path = require('path');


// Set the public folder as the static directory
app.use(express.static('public'));

// Define a route to handle the index.html file
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.get('/login_page', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'signin.html'));
  });
  
  


app.post("/sign_up", (req, res) => {
    var name = req.body.name;
    var email = req.body.email;
    var phno = req.body.phno;
    var password = req.body.password;
    var data = {
        "name": name,
        "email": email,
        "phno": phno,
        "password": password
    }
    db.collection('users').insertOne(data, (err, collection) => {
        if (err) throw err;
        console.log("Record Inserted Successfully");
    });
     res.sendFile(path.join(__dirname, 'public', 'signup_success.html'));


})

app.listen(3000, () => {
    console.log('http://localhost:3000/');
  });
