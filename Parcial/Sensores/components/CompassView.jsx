import { View, Text, StyleSheet } from "react-native";

export default function CompassView({ heading }) {
  const cardinales = [
    { label: "N", top: 16, left: "50%" },
    { label: "E", top: "50%", left: "84%" },
    { label: "S", top: "84%", left: "50%" },
    { label: "W", top: "50%", left: 16 },
  ];

  return (
    <View style={styles.compassWrap}>
      <View style={styles.compassRing}>
        <View style={styles.compassFace}>
          <View style={[styles.needleHolder, { transform: [{ rotate: `${heading}deg` }] }]}>
            <View style={styles.needle} />
          </View>

          {cardinales.map((item) => (
            <Text
              key={item.label}
              style={[
                styles.cardinal,
                {
                  top: item.top,
                  left: item.left,
                  transform: [{ translateX: item.label === "N" || item.label === "S" ? -8 : 0 }],
                },
              ]}
            >
              {item.label}
            </Text>
          ))}

          <View style={styles.needleCenter} />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  compassWrap: {
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 20,
  },
  compassRing: {
    width: 290,
    height: 290,
    borderRadius: 145,
    backgroundColor: "#f7fbff",
    borderWidth: 8,
    borderColor: "#d7e9f8",
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.15,
    shadowRadius: 10,
    elevation: 8,
  },
  compassFace: {
    width: 250,
    height: 250,
    borderRadius: 125,
    backgroundColor: "#eaf5ff",
    borderWidth: 3,
    borderColor: "#bfdaf4",
    alignItems: "center",
    justifyContent: "center",
    position: "relative",
  },
  needleHolder: {
    position: "absolute",
    width: 250,
    height: 250,
    alignItems: "center",
    justifyContent: "flex-start",
  },
  needle: {
    width: 8,
    height: 110,
    backgroundColor: "#d90429",
    borderRadius: 8,
    marginTop: 18,
    shadowColor: "#000",
    shadowOpacity: 0.2,
    shadowRadius: 6,
    elevation: 6,
  },
  needleCenter: {
    position: "absolute",
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: "#123b5a",
    borderWidth: 4,
    borderColor: "#ffffff",
  },
  cardinal: {
    position: "absolute",
    fontSize: 20,
    fontWeight: "700",
    color: "#123b5a",
    textAlign: "center",
  },
});
