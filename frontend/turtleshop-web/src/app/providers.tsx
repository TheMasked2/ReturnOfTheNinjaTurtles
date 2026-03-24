import React from "react";
import { AuthProvider} from "../shared/auth/AuthContext.tsx";

// Later you can import:
// import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
// import { AuthProvider } from "../features/auth/context/AuthProvider";

type Props = { children: React.ReactNode };

// const queryClient = new QueryClient();

export function AppProviders({ children }: Props) {
    return <AuthProvider>{children}</AuthProvider>;
}
