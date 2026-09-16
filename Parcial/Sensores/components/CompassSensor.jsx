import { useState, useEffect } from "react";
import { View, Text, StyleSheet } from "react-native";
import { Magnetometer } from "expo-sensors";
import CompassView from "./CompassView";

const TARGET_COORDS = "19°09'58.45\"N 96°07'45.20\"W";

export default function CompassSensor() {
  const [datos, setDatos] = useState({ x: 0, y: 0, z: 0 });
  const [heading, setHeading] = useState(0);

  useEffect(() => {
    const suscripcion = Magnetometer.addListener((mediciones) => {
      setDatos(mediciones);

      // En una brújula clásica: 0° = Norte, 90° = Este, 180° = Sur, 270° = Oeste.
      const angle = (Math.atan2(mediciones.y, mediciones.x) * 180) / Math.PI;
      const normalized = (90 - angle + 360) % 360;
      setHeading(normalized);
    });

    Magnetometer.setUpdateInterval(100);

    return () => {
      suscripcion.remove();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Brújula</Text>
      <Text style={styles.subtitle}>Sin geolocalización</Text>

      <CompassView heading={heading} />

      <View style={styles.dataBox}>
        <Text style={styles.label}>Orientación</Text>
        <Text style={styles.value}>{Math.round(heading)}°</Text>
        <Text style={styles.target}>Objetivo: {TARGET_COORDS}</Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.axis}>X</Text>
        <Text style={styles.valueSmall}>{datos.x.toFixed(2)}</Text>
      </View>
      <View style={styles.card}>
        <Text style={styles.axis}>Y</Text>
        <Text style={styles.valueSmall}>{datos.y.toFixed(2)}</Text>
      </View>
      <View style={styles.card}>
        <Text style={styles.axis}>Z</Text>
        <Text style={styles.valueSmall}>{datos.z.toFixed(2)}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    padding: 25,
    backgroundColor: "#edf7ff",
  },
  title: {
    fontSize: 30,
    fontWeight: "800",
    textAlign: "center",
    color: "#123b5a",
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 14,
    textAlign: "center",
    color: "#4a6985",
    marginBottom: 18,
  },
  dataBox: {
    backgroundColor: "#ffffff",
    borderRadius: 18,
    padding: 16,
    marginBottom: 18,
    alignItems: "center",
  },
  label: {
    fontSize: 14,
    color: "#52708e",
    textTransform: "uppercase",
    letterSpacing: 1,
  },
  value: {
    fontSize: 32,
    fontWeight: "800",
    color: "#0d3f60",
    marginTop: 4,
  },
  target: {
    marginTop: 6,
    fontSize: 12,
    color: "#466781",
    textAlign: "center",
  },
  card: {
    backgroundColor: "#fff",
    padding: 18,
    marginBottom: 12,
    borderRadius: 16,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  axis: {
    fontSize: 18,
    fontWeight: "700",
    color: "#2564eb",
  },
  valueSmall: {
    fontSize: 18,
    fontWeight: "700",
    color: "#2564eb",
  },
});
