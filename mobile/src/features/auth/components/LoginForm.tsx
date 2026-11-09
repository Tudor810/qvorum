// View: renders the form and forwards user input to the view model.
import { strings } from '@qvorum/shared';
import { useState } from 'react';
import { AppText, Button, Card, Field } from '@/components/ui';
import { useLogin } from '../hooks/useLogin';

export function LoginForm() {
  const { loading, error, submit } = useLogin();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  return (
    <Card style={{ width: '100%' }}>
      <AppText variant="title">{strings.auth.title}</AppText>
      <Field
        label={strings.auth.email}
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
        autoComplete="email"
      />
      <Field
        label={strings.auth.password}
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        autoComplete="password"
      />
      {error ? <AppText color="danger">{error}</AppText> : null}
      <Button title={strings.auth.logIn} loading={loading} onPress={() => void submit({ email, password })} />
    </Card>
  );
}
