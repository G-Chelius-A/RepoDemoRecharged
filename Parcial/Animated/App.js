import { useEffect, useRef } from "react";
import { Animated, View, Text, Image } from 'react-native';


export default function App() {
  const opacity = useRef(new Animated.Value(0)).current;
  const position = useRef(new Animated.Value(250)).current;
  const positionX = useRef(new Animated.Value(-250)).current;
  const escala = useRef(new Animated.Value(0)).current;


    {/*
    Animated.timing(opacity,{
      toValue: 1,
      duration: 5000,
      useNativeDriver: true
    }).start();
    */}

    /* Animated.timing(position,{
      toValue: 0,
      duration: 5000,
      useNativeDriver: true
    }).start();  */

/*    Animated.timing(escala,{
      toValue: 1,
      duration: 5000,
      useNativeDriver: true,
    }).start();
  }, []);

  useEffect(()=>{
    Animated.timing(positionX,{
      toValue: 20,
      duration: 7500,
      useNativeDriver: true
    }).start();
  }, []);

    useEffect(()=>{
    Animated.timing(position,{
      toValue: -300,
      duration: 5000,
      useNativeDriver: true
    }).start();
  }, []);

  useEffect(()=>{
    Animated.timing(opacity,{
      toValue: 1,
      duration: 7500,
      useNativeDriver: true
    }).start();
  }, []);  */

useEffect(() => {
    Animated.parallel([
      Animated.timing(opacity,{
      toValue: 1,
      duration: 7500,
      useNativeDriver: true
    }),

    Animated.timing(position,{
      toValue: -300,
      duration: 8000,
      useNativeDriver: true
    }),

    Animated.timing(positionX,{
      toValue: 20,
      duration: 7500,
      useNativeDriver: true
    }),

    Animated.timing(escala,{
      toValue: 1,
      duration: 5000,
      useNativeDriver: true,
    }),
    ]).start();
  }, []);

  return(
    <View style={{
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: '#86cadf',
    }}>
      
      <Animated.Text
        style={{
          fontSize:120,
          opacity: opacity,
          transform: [{translateY:position}, {translateX:positionX}, {scaleX:escala}, {scaleY:escala}],
          
        }}>
          🚀
      </Animated.Text>
    </View>
  );
}

// Se pueden usar imágenes en el Animated Text