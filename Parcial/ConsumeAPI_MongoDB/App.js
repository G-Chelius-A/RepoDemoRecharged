import React, { useState, useEffect } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Image,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import LoginScreen from './components/LoginScreen';
import MovieModal from './components/MovieModal';

export default function App() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [loggedInUser, setLoggedInUser] = useState(null);

  useEffect(() => {
    if (!loggedInUser) {
      return;
    }

    fetch("https://turret-prepay-germinate.ngrok-free.dev/movies", {
      headers: {
        Accept: 'application/json',
        'ngrok-skip-browser-warning': 'true',
      },
    })
      .then((res) => res.json())
      .then((data) => {
        setMovies(data);
        setLoading(false);
      })
      .catch((error) => console.error(error));
  }, [loggedInUser]);

  if (!loggedInUser) {
    return <LoginScreen onLogin={setLoggedInUser} />;
  }

  if(loading){
    return(
      <View style={styles.loader}>
        <ActivityIndicator size="large" color="#000000"/>
      </View>
    );
  }

  const renderItem = ({ item }) => {
    return (
      <Pressable
        accessibilityLabel={`Ver detalles de ${item.title}`}
        accessibilityRole="button"
        onPress={() => setSelectedMovie(item)}
        style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}
      >
        {item.poster ? ( // Las imágenes remotas de internet requieren obligatoriamente dimensiones en React Native
          <Image source={{ uri: item.poster }} style={styles.poster} />
        ) : (
          <View style={[styles.poster, styles.noImageContainer]}>
            <Text style={styles.noImageText}>No Image</Text>
          </View>
        )}
        <View style={styles.infoContainer}>
          <Text style={styles.title} numberOfLines={2}>{item.title}</Text>
          <Text style={styles.plot} numberOfLines={4}>
            {item.fullplot || "Sin descripción disponible."}
          </Text>
        </View>
      </Pressable>
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.headerTitle}> CARTELERA: ~ SAMPLE MFLIX ~ </Text>
      <FlatList
        data={movies}
        keyExtractor={(item) => item._id.toString()}
        renderItem={renderItem}
        contentContainerStyle={styles.listContainer}
      />
      <MovieModal
        movie={selectedMovie}
        visible={selectedMovie !== null}
        onClose={() => setSelectedMovie(null)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#93beff',
    paddingTop: 50,
  },
  loader: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    color: '#be1818',
    backgroundColor: '#c0b67a'
  },
  centerContainer: {
    flex: 1,
    backgroundColor: '#F5F7FA',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    color: '#666',
    fontSize: 16,
  },
  headerTitle: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#000000',
    textAlign: 'center',
    fontWeight: 'bold',
    marginBottom: 15,
    backgroundColor: '#ffbb00',
    padding: 15,
  },
  listContainer: {
    paddingHorizontal: 15,
    paddingBottom: 20,
  },
  card: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    marginBottom: 15,
    padding: 12,
  },
  cardPressed: {
    opacity: 0.78,
    transform: [{ scale: 0.98 }],
  },
  poster: {
    width: 90,
    height: 130,
    borderRadius: 8,
    backgroundColor: '#E1E5EB',
  },
  noImageContainer: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  noImageText: {
    color: '#888',
    fontSize: 12,
    fontWeight: '600',
  },
  infoContainer: {
    flex: 1,
    paddingLeft: 15,
    justifyContent: 'flex-start',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1A1A1A',
    marginBottom: 6,
  },
  plot: {
    fontSize: 14,
    color: '#666666',
    lineHeight: 18,
  },
});
