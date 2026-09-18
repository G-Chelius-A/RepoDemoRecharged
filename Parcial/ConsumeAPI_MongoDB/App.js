import React, { useState, useEffect } from 'react';
import { StyleSheet, Text, View, ActivityIndicator, FlatList, Image } from 'react-native';

export default function App() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(()=>{
    fetch("https://turret-prepay-germinate.ngrok-free.dev/movies")
    .then((res)=>res.json())
    .then((data) => {
      setMovies(data);
      setLoading(false);
    })
    .catch((error)=>console.error(error))
  },[]);

  if(loading){
    return(
      <View style={styles.container}>
        <ActivityIndicator size="large" color="#007AFF"/>
      </View>
    );
  }

  const renderItem = ({item}) => {
      return (
        <View>
          {item.poster ? (
            // Las imágenes remotas de internet requieren obligatoriamente dimensiones en React Native
            <Image source={{uri:item.poster}} style={{width: 100, height: 150}}/>
          ):(
            <View>
              <Text>No Image</Text>
            </View>
          )}
           <View>
            <Text>{item.title}</Text>
            <Text>{item.fullplot || "Sin descripción"}</Text>
           </View>        
        </View>
      );
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={movies}
        keyExtractor={(item)=>item._id.toString()}
        renderItem={renderItem}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
