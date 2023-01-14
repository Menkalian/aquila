import axios from "axios"
import type {UserData} from "@/data/CommonTypes";

function getCurrentUser() {
    axios.create({
                     baseURL: "http://localhost:8080"
                 })
        .get<UserData>("/user/me")
}