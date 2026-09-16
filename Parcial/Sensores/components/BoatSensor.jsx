import { useEffect, useState } from "react";
import { View, StyleSheet, Text, Image } from "react-native";
import { Gyroscope } from "expo-sensors";

const primaryImageUri = "https://upload.wikimedia.org/wikipedia/commons/8/88/Ship_diagram-numbers.svg?utm_source=es.wikipedia.org&utm_campaign=index&utm_content=original";
const fallbackImageUri = "https://images.unsplash.com/photo-1593536284003-ef3103cff953?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTh8fGJvYXRpbmd8ZW58MHx8MHx8fDA%3D";

export default function BoatSensor() {
  const [motion, setMotion] = useState({ x: 0, y: 0, z: 0 });
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    const subscription = Gyroscope.addListener((datos) => {
      setMotion(datos);
    });

    Gyroscope.setUpdateInterval(100);

    return () => {
      subscription.remove();
    };
  }, []);

  const tilt = motion.x * 18;
  const bob = motion.y * 18;
  const roll = motion.z * 10;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Barquito</Text>

      <View style={styles.scene}>
        <View style={styles.water} />
        <View style={[styles.ripple, styles.rippleA]} />
        <View style={[styles.ripple, styles.rippleB]} />
        <View style={[styles.ripple, styles.rippleC]} />
        <View style={[styles.ripple, styles.rippleD]} />

        <View
          style={[
            styles.boatWrapper,
            {
              transform: [
                { translateY: bob },
                { rotate: `${tilt}deg` },
              ],
            },
          ]}
        >
          <View style={styles.boatShadow} />
          <Image
            source={{ uri: imageError ? fallbackImageUri : primaryImageUri }}
            style={[
              styles.image,
              {
                transform: [{ rotate: `${roll}deg` }],
              },
            ]}
            resizeMode="contain"
            onError={() => setImageError(true)}
          />
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
    backgroundColor: "#dff6ff",
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: "800",
    color: "#0d3c64",
    marginBottom: 18,
  },
  scene: {
    width: "100%",
    height: 420,
    maxWidth: 420,
    borderRadius: 28,
    backgroundColor: "#bfeaf8",
    overflow: "hidden",
    justifyContent: "center",
    alignItems: "center",
    position: "relative",
  },
  water: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    height: 170,
    backgroundColor: "#39b9e6",
    borderTopLeftRadius: 32,
    borderTopRightRadius: 32,
  },
  ripple: {
    position: "absolute",
    left: -20,
    right: -20,
    borderRadius: 999,
    backgroundColor: "rgba(255,255,255,0.28)",
    borderWidth: 2,
    borderColor: "rgba(255,255,255,0.18)",
  },
  rippleA: {
    bottom: 38,
    height: 44,
  },
  rippleB: {
    bottom: 70,
    height: 34,
    backgroundColor: "rgba(255,255,255,0.22)",
  },
  rippleC: {
    bottom: 96,
    height: 24,
    backgroundColor: "rgba(255,255,255,0.18)",
  },
  rippleD: {
    bottom: 122,
    height: 16,
    backgroundColor: "rgba(255,255,255,0.12)",
  },
  boatWrapper: {
    position: "absolute",
    alignItems: "center",
    justifyContent: "center",
    bottom: 90,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
    elevation: 8,
  },
  boatShadow: {
    position: "absolute",
    bottom: -20,
    width: 180,
    height: 20,
    backgroundColor: "rgba(0,0,0,0.15)",
    borderRadius: 20,
  },
  image: {
    width: 210,
    height: 140,
    borderRadius: 22,
    backgroundColor: "#dfeef7",
    borderWidth: 0,
  },
  infoCard: {
    width: "100%",
    maxWidth: 420,
    marginTop: 18,
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: "rgba(255,255,255,0.8)",
    borderRadius: 18,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  reading: {
    fontSize: 16,
    fontWeight: "700",
    color: "#164e63",
  },
});
