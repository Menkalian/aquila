export interface PaginatedPluginData {
    plugins: PluginData[],
    start: number,
    amount: number,
    nextPage: string
}

export interface PluginData {
    id: string,
    name: string,
    description: string,
    versiontext: string,
    version: VersionData,
    archived?: boolean,
    creator: number,
    createdAt: string,
    tags: string[],
    artifactUrl: string
}

export interface PluginFilterData {
    searchterm?: string,
    creator?: number,
    tags?: string[],
    timerange?: TimeRangeData
}

export interface PluginUploadData {
    id: string,
    name: string,
    description: string,
    versiontext: string,
    version: VersionData,
    archived?: boolean,
    creator: number,
    createdAt: string,
    tags: string[],
    artifactData: number[]
}

export interface TimeRangeData {
    from: string,
    until: string
}

export interface UserData {
    userId: number,
    name: string,
    avatarUrl?: string
}

export interface UserData {
    userId: number,
    name: string,
    avatarData?: number[]
}

export interface VersionData {
    major: number,
    minor?: number,
    patch?: number
}
