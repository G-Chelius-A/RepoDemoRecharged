import 'react-native-url-polyfill/auto';
import { createClient } from '@supabase/supabase-js';
import AsyncStorage from '@react-native-async-storage/async-storage';

const supabaseUrl = 'https://whvorrzlzqpymbzlltiw.supabase.co';
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indodm9ycnpsenFweW1iemxsdGl3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM1NDk0MDEsImV4cCI6MjA4OTEyNTQwMX0.nQfd_fC9l2V8iceMQ45WgpjC-zyMnoHucJogOTPfOMw';

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: false,
  },
});