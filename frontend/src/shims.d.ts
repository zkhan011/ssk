declare module 'react' {
  export const createContext: any;
  export const useContext: any;
  export const useEffect: any;
  export const useState: any;
  export const Fragment: any;
  const React: any;
  export default React;
}
declare module 'react-dom/client' { export const createRoot: any; }
declare module 'react/jsx-runtime' { export const jsx: any; export const jsxs: any; export const Fragment: any; }
declare module '*.css';
declare namespace JSX { interface IntrinsicElements { [elemName: string]: any } }
