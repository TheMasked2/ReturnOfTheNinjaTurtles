import React from "react";
import { AuthProvider} from "../shared/auth/AuthContext.tsx";

type Props = { children: React.ReactNode };

export function AppProviders({ children }: Props) {
    return <AuthProvider>{children}</AuthProvider>;
}
