// View: renders the form and forwards user input to the view model.
import { colors, spacing, strings } from '@qvorum/shared';
import { useState } from 'react';
import { Button, StyleSheet, Text, TextInput, View } from 'react-native';
import { useLogin } from '../hooks/useLogin';

export function LoginForm() {
  const { loading, error, submit } = useLogin();
  const [email, setEmail] = useState('');
  const [parola, setParola] = useState('');

  return (
    <View style={styles.form}>
      <TextInput
        style={styles.input}
        placeholder={strings.auth.email}
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
      />
      <TextInput
        style={styles.input}
        placeholder={strings.auth.parola}
        value={parola}
        onChangeText={setParola}
        secureTextEntry
      />
      {error && <Text style={styles.error}>{error}</Text>}
      <Button
        title={strings.auth.conectare}
        disabled={loading}
        onPress={() => void submit({ email, parola })}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  form: {
    width: '100%',
    gap: spacing.md,
  },
  input: {
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
  },
  error: {
    color: colors.danger,
  },
});
