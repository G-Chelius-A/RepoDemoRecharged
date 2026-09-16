import { useState, useEffect } from "react";
import { Text, View, StyleSheet } from "react-native";
import { Magnetometer } from "expo-sensors";

export default function MagnetometerSensor() {
    const [datos, setDatos] = useState({ x: 0, y: 0, z: 0 });

    useEffect(() => {
        const suscripcion = Magnetometer.addListener((mediciones) => {
            setDatos(mediciones);
        });

        Magnetometer.setUpdateInterval(100);

        return () => {
            suscripcion.remove();
        };
    }, []);

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Magnetómetro</Text>
            <View style={styles.card}>
                <Text style={styles.axis}>X</Text>
                <Text style={styles.value}>{datos.x.toFixed(2)}</Text>
            </View>
            <View style={styles.card}>
                <Text style={styles.axis}>Y</Text>
                <Text style={styles.value}>{datos.y.toFixed(2)}</Text>
            </View>
            <View style={styles.card}>
                <Text style={styles.axis}>Z</Text>
                <Text style={styles.value}>{datos.z.toFixed(2)}</Text>
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
    title: {
        fontSize: 28,
        fontWeight: "bold",
        textAlign: "center",
        marginBottom: 30,
        color: "#1e344f",
    },
    card: {
        backgroundColor: "#fff",
        padding: 20,
        marginBottom: 15,
        borderRadius: 20,
        flexDirection: "row",
        justifyContent: "space-between",
    },
    axis: {
        fontSize: 22,
        fontWeight: "bold",
        color: "#2564eb",
    },
    value: {
        fontSize: 22,
        fontWeight: "bold",
        color: "#2564eb",
    },
});