import { useState, useEffect } from "react";
import { Text, View, StyleSheet } from "react-native";
import { Pedometer } from "expo-sensors";

export default function PedometerSensor(){
    const [pasos, setPasos] = useState(0);
    const [disponible, setDisponible] = useState(false);

    useEffect(()=>{
        let suscripcion;
        const iniciar = async () => {
            // 1.- Comprobar la existencia del podómetro
            const disponible = await Pedometer.isAvailableAsync();
            setDisponible(disponible);

            if (disponible) {
                // 2.- SOLICITAR PERMISOS EXPLÍCITOS (Obligatorio en Android)
                const { status } = await Pedometer.requestPermissionsAsync();
                
                if (status === 'granted') {
                    // 3.- Suscribirnos al podómetro si el permiso fue concedido
                    suscripcion = Pedometer.watchStepCount((resultado)=>{
                        setPasos(resultado.steps);
                    });
                } else {
                    console.log("El usuario denegó el permiso de Actividad Física");
                }
            }
        }

        iniciar();

        // 4.- Cancelar la suscripción
        return () => {
            if(suscripcion){
                suscripcion.remove();
            }
        }
    }, []);


    return(
        <View style={styles.container}>
            <Text style={styles.titulo}>Contador de Pasos</Text>
            <Text style={styles.axis}>{disponible ? "Disponible" : "No disponible"}</Text>
            <View style={styles.card}>
                <Text style={styles.axis}>Pasos</Text>
                <Text style={styles.value}>{pasos}</Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: "center",
        padding: 25,
        backgroundColor: "#f6f6f6",
    },
    titulo:{
        fontSize: 28,
        fontWeight: "bold",
        textAlign: "center",
        marginBottom: 30,
        color: "#1e344f",
    },
    card:{
        backgroundColor: "#fff",
        padding: 20,
        marginBottom: 15,
        borderRadius: 20,
        flexDirection: 'row',
        justifyContent: "space-between",
    },
    axis:{
        fontSize: 22,
        fontWeight: 'bold',
        color: "#2564eb",
    },
    value:{
        fontSize: 22,
        fontWeight: 'bold',
        color: "#2564eb",
    },

});