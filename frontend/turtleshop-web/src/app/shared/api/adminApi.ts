
import { useEffect, useState } from "react";
import type { User } from "../../../shared/auth/AuthContext";

export interface Product { id:string; name:string; description:string; price:number; stock:number; imageUrl:string; }
export interface InventoryItem { id:string; product:Product; stock:number; location:string; }
export interface Transaction { id:string; orderId:string; amount:number; date:string; status:string; }
export interface Order { id:string; customer:User; date:string; total:number; status:string; }

const mem:any={users:[],products:[],inventory:[],transactions:[],orders:[]};

function useList(key:string){ const [data,setData]=useState(mem[key]||[]); useEffect(()=>{setData(mem[key]||[])},[]); return {data,isLoading:false};}
const makeCreate=(key:string)=>()=>[async (item:any)=>{ item={...item,id:item.id||String(Date.now())}; mem[key].push(item); return item;}];
const makeUpdate=(key:string)=>()=>[async (item:any)=>{ mem[key]=mem[key].map((x:any)=>x.id===item.id?item:x); return item;}];
const makeDelete=(key:string)=>()=>[async (id:string)=>{ mem[key]=mem[key].filter((x:any)=>x.id!==id); return {success:true,id};}];

export const useGetUsersQuery=()=>useList("users");
export const useCreateUserMutation=makeCreate("users");
export const useUpdateUserMutation=makeUpdate("users");
export const useDeleteUserMutation=makeDelete("users");

export const useGetProductsQuery=()=>useList("products");
export const useCreateProductMutation=makeCreate("products");
export const useUpdateProductMutation=makeUpdate("products");
export const useDeleteProductMutation=makeDelete("products");

export const useGetInventoryQuery=()=>useList("inventory");
export const useAddInventoryMutation=makeCreate("inventory");
export const useUpdateInventoryMutation=makeUpdate("inventory");
export const useDeleteInventoryMutation=makeDelete("inventory");

export const useGetTransactionsQuery=()=>useList("transactions");
export const useCreateTransactionMutation=makeCreate("transactions");
export const useUpdateTransactionMutation=makeUpdate("transactions");
export const useDeleteTransactionMutation=makeDelete("transactions");

export const useGetOrdersQuery=()=>useList("orders");
export const useCreateOrderMutation=makeCreate("orders");
export const useUpdateOrderMutation=makeUpdate("orders");
export const useDeleteOrderMutation=makeDelete("orders");
