const express = require('express');
const { MongoClient } = require('mongodb');
const cors = require("cors");

const dns = require('node:dns/promises');
dns.setServers(["1.1.1.1", "8.8.8.8"]);

const app = express();
app.use(cors());
app.use(express.json());

const uri = "mongodb+srv://201120009_db_user:pincheMongoDB@cluster0.43iaflv.mongodb.net/?appName=Cluster0"
const client = new MongoClient(uri);

async function main(){
    await client.connect();
    const db = client.db("sample_mflix");
    const movies = db.collection("movies");
    const loginDb = client.db("login_service");
    const users = loginDb.collection("users");

    app.post("/register", async(req, res)=>{
        try {
            const { email, password } = req.body;

            if (!email || !password) {
                return res.status(400).json({ error: "El correo y la contraseña son obligatorios." });
            }

            const existingUser = await users.findOne({ email });
            if (existingUser) {
                return res.status(409).json({ error: "El usuario ya existe." });
            }

            await users.insertOne({ email, password });
            return res.status(201).json({ message: "Usuario creado correctamente." });
        } catch (error) {
            console.error("Error al registrar usuario:", error);
            return res.status(500).json({ error: "No se pudo registrar el usuario." });
        }
    });

    app.post("/login", async(req, res)=>{
        try {
            const { email, password } = req.body;
            const user = await users.findOne({ email, password });

            if (!user) {
                return res.status(401).json({ error: "Correo o contraseña incorrectos." });
            }

            return res.json({
                message: "Inicio de sesión correcto.",
                user: { id: user._id, email: user.email },
            });
        } catch (error) {
            console.error("Error al iniciar sesión:", error);
            return res.status(500).json({ error: "No se pudo iniciar sesión." });
        }
    });

    app.get("/movies", async(req, res)=>{
        try {
            // CORRECCIÓN: Pasamos el filtro vacío {} y las opciones de proyección juntas
            const data = await movies
            .find({}, { projection: { poster: 1, title: 1, fullplot: 1 } })
            .limit(60)
            .toArray();
            
            res.json(data);
        } catch (error) {
            console.error("Error en la consulta:", error);
            res.status(500).json({ error: "Error al obtener datos" });
        }
    });

    app.listen(4000,()=>console.log("Server running at http://localhost:4000"))
}

main().catch(console.error);
