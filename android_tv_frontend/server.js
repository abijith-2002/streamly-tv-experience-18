const https = require("https");
const fs = require("fs");
const express = require("express");
const app = express();

app.use(express.static("/path/to/files"));

https.createServer({
  key: fs.readFileSync("ssl/key.pem"),
  cert: fs.readFileSync("ssl/cert.pem")
}, app).listen(4433, () => {
  console.log("HTTPS file server running on port 4433");
});

