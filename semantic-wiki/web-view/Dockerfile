# Use an official Node.js runtime as the base image
FROM node:14-alpine

# Copy package.json and package-lock.json to the container
COPY . /viz-app

# Set the working directory in the container
WORKDIR /viz-app

# Install dependencies
RUN npm install

# Build the frontend app
RUN npm run build

# Set the command to run the frontend app
CMD [ "npm", "start"]
