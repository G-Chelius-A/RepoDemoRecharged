import { View, ImageBackground, StyleSheet, Dimensions, Image, Text } from 'react-native';

export default function ImagenFondo() {


    return(
        <View>
            <ImageBackground
                style={styles.fondo}
                source={require('../assets/DoctorDoom.jpg')}
            > 
                <View style={styles.container}>
                    <View style={styles.contains}> 
                       <Text style={styles.texto}>DOOM</Text>                        
                    </View>
                    <Image
                        source={{uri:'https://image.tmdb.org/t/p/original/ncgHqZlYPObQ0y6fMoh1ibQnqgt.png'}}
                        style={styles.foto}
                    />
                </View>
            </ImageBackground>
        </View>
    );
}

const styles = StyleSheet.create({
    
    fondo: {
        height: '100%',
        width: Dimensions.get("window").width ,
    },
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        
    },
    contains: {
        backgroundColor: '#0538087c',
        width: Dimensions.get("window").width,
    },
    foto: {
        width: undefined,
        height: 120,
        borderRadius: 26,
        boderWidth: 10,
        borderColor: '#fff',
        shadowColor: '#cceb44d8',
        shadowOffset: {width: 0, height: 20},
        shadowRadius: 10,
        elevation: 40,
        aspectRatio: 25/9,
    },
    texto: {
        fontSize: Dimensions.get("window").width*0.32,
        color: '#fff',
        alignItems: 'center',
    },
});
// flex:1 y height: '100%' para que se extienda por toda la pantalla.

/*
    foto: {
        margin: 20,
        width: 260,
        height: 260,
        borderRadius: 16,
        boderWidth: 10,
        borderColor: '#fff',
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 6},
        shadowRadius: 10,
        elevation: 8,
    },

        foto: {
        width: '100%',
        height: undefined, // Permite escalar dinámicamente
        aspectRatio: 19 / 9, // Permite el ratio
    },

*/