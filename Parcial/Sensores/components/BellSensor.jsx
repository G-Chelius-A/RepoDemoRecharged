import { useEffect, useState } from "react";
import { View, Text, StyleSheet } from "react-native";
import { Accelerometer } from "expo-sensors";

export default function BellSensor() {
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

  const offsetX = motion.x * 100;
  const offsetY = motion.y * -80;
  const rotation = motion.x * 28;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Campana de México</Text>

      <View style={styles.scene}>
        <View style={styles.flagStrip}>
          <View style={[styles.strip, styles.green]} />
          <View style={[styles.strip, styles.white]} />
          <View style={[styles.strip, styles.red]} />
        </View>

        <View
          style={[
            styles.bellWrapper,
            {
              transform: [
                { translateX: offsetX },
                { translateY: offsetY },
                { rotate: `${rotation}deg` },
              ],
            },
          ]}
        >
          <View style={styles.topCap} />
          <View style={styles.bellBody}>
            <View style={styles.bellBand} />
            <View style={styles.bellMiddle} />
            <View style={styles.clapper} />
          </View>
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
    backgroundColor: "#f8f8f8",
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: "800",
    color: "#0a3d2f",
    marginBottom: 18,
  },
  scene: {
    width: "100%",
    height: 420,
    maxWidth: 420,
    borderRadius: 28,
    backgroundColor: "#f7f4e8",
    borderWidth: 2,
    borderColor: "#d7d7d7",
    justifyContent: "center",
    alignItems: "center",
    overflow: "hidden",
    position: "relative",
  },
  flagStrip: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    height: 18,
    flexDirection: "row",
  },
  strip: {
    flex: 1,
  },
  green: {
    backgroundColor: "#006847",
  },
  white: {
    backgroundColor: "#ffffff",
  },
  red: {
    backgroundColor: "#ce1126",
  },
  bellWrapper: {
    position: "absolute",
    alignItems: "center",
    justifyContent: "center",
  },
  topCap: {
    width: 120,
    height: 28,
    backgroundColor: "#006847",
    borderTopLeftRadius: 18,
    borderTopRightRadius: 18,
    borderBottomLeftRadius: 8,
    borderBottomRightRadius: 8,
    borderWidth: 3,
    borderColor: "#004d36",
  },
  bellBody: {
    width: 150,
    height: 170,
    backgroundColor: "#ce1126",
    borderBottomLeftRadius: 80,
    borderBottomRightRadius: 80,
    borderTopLeftRadius: 40,
    borderTopRightRadius: 40,
    justifyContent: "flex-end",
    alignItems: "center",
    borderWidth: 5,
    borderColor: "#8d0d1d",
    overflow: "hidden",
  },
  bellBand: {
    position: "absolute",
    top: 32,
    width: 110,
    height: 18,
    backgroundColor: "#ffffff",
    borderRadius: 12,
  },
  bellMiddle: {
    width: 100,
    height: 80,
    backgroundColor: "#006847",
    borderRadius: 40,
    marginBottom: 18,
    borderWidth: 4,
    borderColor: "#ffffff",
  },
  clapper: {
    width: 28,
    height: 28,
    backgroundColor: "#f5f5f5",
    borderRadius: 14,
    marginBottom: 12,
    borderWidth: 4,
    borderColor: "#ce1126",
  },
  infoCard: {
    width: "100%",
    maxWidth: 420,
    marginTop: 18,
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: "rgba(255,255,255,0.82)",
    borderRadius: 18,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  reading: {
    fontSize: 16,
    fontWeight: "700",
    color: "#0e3b2b",
  },
});
