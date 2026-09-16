import { useEffect, useState } from "react";
import { View, Text, StyleSheet } from "react-native";
import { Accelerometer } from "expo-sensors";

export default function AirplaneSensor() {
  const [motion, setMotion] = useState({ x: 0, y: 0, z: 0 });

  useEffect(() => {
    const subscription = Accelerometer.addListener((datos) => {
      setMotion(datos);
    });

    Accelerometer.setUpdateInterval(100);

    return () => {
      subscription.remove();
    };
  }, []);

  const offsetX = motion.x * 110;
  const offsetY = motion.y * -90;
  const rotation = motion.x * 30;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Avión en vuelo</Text>

      <View style={styles.scene}>
        <View style={[styles.cloud, styles.cloud1]} />
        <View style={[styles.cloud, styles.cloud2]} />
        <View style={[styles.cloud, styles.cloud3]} />

        <View
          style={[
            styles.airplaneContainer,
            {
              transform: [
                { translateX: offsetX },
                { translateY: offsetY },
                { rotate: `${rotation}deg` },
              ],
            },
          ]}
        >
          <Text style={styles.airplane}>✈️</Text>
        </View>
      </View>

      <View style={styles.infoCard}>
        <Text style={styles.reading}>X: {motion.x.toFixed(2)}</Text>
        <Text style={styles.reading}>Y: {motion.y.toFixed(2)}</Text>
        <Text style={styles.reading}>Z: {motion.z.toFixed(2)}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    width: "100%",
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#dff4ff",
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: "700",
    color: "#163f6f",
    marginBottom: 18,
  },
  scene: {
    width: "100%",
    height: 420,
    maxWidth: 420,
    borderRadius: 28,
    backgroundColor: "linear-gradient(180deg, #89d4ff 0%, #c8f0ff 60%, #f9f7d8 100%)",
    overflow: "hidden",
    justifyContent: "center",
    alignItems: "center",
    borderWidth: 2,
    borderColor: "#cfefff",
    position: "relative",
  },
  cloud: {
    position: "absolute",
    backgroundColor: "rgba(255,255,255,0.8)",
    borderRadius: 50,
  },
  cloud1: {
    width: 90,
    height: 28,
    top: 55,
    left: 40,
  },
  cloud2: {
    width: 110,
    height: 32,
    top: 110,
    right: 40,
  },
  cloud3: {
    width: 80,
    height: 26,
    bottom: 110,
    left: 80,
  },
  airplaneContainer: {
    position: "absolute",
    alignItems: "center",
    justifyContent: "center",
  },
  airplane: {
    fontSize: 84,
    textAlign: "center",
    transform: [{ rotate: "-10deg" }],
  },
  infoCard: {
    width: "100%",
    maxWidth: 420,
    marginTop: 18,
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: "rgba(255,255,255,0.75)",
    borderRadius: 18,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  reading: {
    fontSize: 16,
    color: "#1e3a5f",
    fontWeight: "600",
  },
});
